package zame.game.providers;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.os.AsyncTask;
import android.os.Build;
import java.io.File;
import java.io.FileOutputStream;
import org.holoeverywhere.preference.SharedPreferences;
import zame.game.Common;
import zame.game.MyApplication;
import zame.game.R;
import zame.game.engine.TextureLoader;

public class CachedTexturesProvider {
	public static final String BROADCAST_ACTION = "local:CachedTexturesProvider";
	public static final String EXTRA_PROGRESS = "EXTRA_PROGRESS";
	protected static final int CACHE_VERSION = 5;

	protected static final ColorMatrixColorFilter GRAY_COLOR_FILTER = new ColorMatrixColorFilter(new ColorMatrix(new float[] {
		0, 0, 0, 0, 0,
		0, 0, 0, 0, 0,
		0, 0, 0, 0, 0,
		1, 0, 0, 0, 0,
	}));

	public static final int[][] mainTexMap = new int[][] {
		{ R.drawable.texmap_1, 0 },
		{ R.drawable.texmap_2, 0 },
		{ R.drawable.texmap_3, 0 },
		{ R.drawable.texmap_4, 0 },
		{ R.drawable.texmap_5, 0 },
	};

	protected static final int[][] monTexMap = new int[][] {
		{ R.drawable.texmap_mon_1_p, R.drawable.texmap_mon_1_a },
		{ R.drawable.texmap_mon_2_p, R.drawable.texmap_mon_2_a },
		{ R.drawable.texmap_mon_3_p, R.drawable.texmap_mon_3_a },
		{ R.drawable.texmap_mon_4_p, R.drawable.texmap_mon_4_a },

		{ R.drawable.texmap_mon_5_p, R.drawable.texmap_mon_5_a },
		{ R.drawable.texmap_mon_6_p, R.drawable.texmap_mon_6_a },
		{ R.drawable.texmap_mon_7_p, R.drawable.texmap_mon_7_a },
		{ R.drawable.texmap_mon_8_p, R.drawable.texmap_mon_8_a },
	};

	private CachedTexturesProvider() {
	}

	public interface IOnComplete {
		void onComplete();
	}

	public static class Task extends AsyncTask<Void,Integer,Boolean> {
		protected IOnComplete onComplete;
		protected Resources resources;
		protected BitmapFactory.Options bitmapOptions;
		protected int totalCount = 0;
		protected int cachedCount = 0;

		public Task(IOnComplete onComplete) {
			this.onComplete = onComplete;
			this.resources = MyApplication.self.getResources();

			bitmapOptions = new BitmapFactory.Options();
			bitmapOptions.inDither = false;
			bitmapOptions.inScaled = false;
			bitmapOptions.inPurgeable = false;
			bitmapOptions.inInputShareable = false;

			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
				// пробуем уменьшить расход памяти, ибо всё равно потом все битмапы пургаются и за-null-иваются
				bitmapOptions.inMutable = true;
			}
		}

		protected Bitmap loadAlphaImage(int pixelsResId, int alphaResId) {
			Bitmap pixelsBitmap = decodeResource(resources, pixelsResId, bitmapOptions);

			if (alphaResId == 0) {
				return pixelsBitmap;
			}

			Bitmap result = Common.createBitmap(pixelsBitmap.getWidth(), pixelsBitmap.getHeight(), "Can't alloc result bitmap for tiles");
			Canvas canvas = new Canvas(result);
			canvas.setDensity(Bitmap.DENSITY_NONE);

			canvas.drawBitmap(pixelsBitmap, 0.0f, 0.0f, null);
			pixelsBitmap.recycle();
			pixelsBitmap = null;

			// http://stackoverflow.com/questions/5098680/how-to-combine-two-opaque-bitmaps-into-one-with-alpha-channel
			Bitmap alphaBitmap = decodeResource(resources, alphaResId, bitmapOptions);

			Paint alphaPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
			alphaPaint.setColorFilter(GRAY_COLOR_FILTER);
			alphaPaint.setXfermode(new PorterDuffXfermode(Mode.DST_IN));
			canvas.drawBitmap(alphaBitmap, 0.0f, 0.0f, alphaPaint);

			alphaBitmap.recycle();
			alphaBitmap = null;

			canvas = null;
			return result;
		}

		protected Bitmap loadMainTexture(int[] texList) {
			Bitmap img = Common.createBitmap(1024, 1024, "Can't alloc bitmap for tiles");
			Canvas canvas = new Canvas(img);
			canvas.setDensity(Bitmap.DENSITY_NONE);

			Bitmap tiles = loadAlphaImage(texList[0], texList[1]);
			canvas.drawBitmap(tiles, 0.0f, (float)(TextureLoader.ROW_TILES * 66), null);
			tiles.recycle();
			tiles = null;

			Bitmap common = loadAlphaImage(R.drawable.texmap_common, 0);
			canvas.drawBitmap(common, 0.0f, (float)(TextureLoader.ROW_COMMON * 66), null);
			common.recycle();
			common = null;

			canvas = null;
			return img;
		}

		protected Bitmap loadMonTexture(int offset) {
			Bitmap img = Common.createBitmap(1024, 1024, "Can't alloc bitmap for monsters");
			Canvas canvas = new Canvas(img);
			canvas.setDensity(Bitmap.DENSITY_NONE);

			for (int i = 0; i < 4; i++) {
				if (i + offset >= monTexMap.length) {
					break;
				}

				Bitmap mon = loadAlphaImage(monTexMap[i + offset][0], monTexMap[i + offset][1]);
				canvas.drawBitmap(mon, 0.0f, (float)(i * 256), null);
				mon.recycle();
				mon = null;
			}

			canvas = null;
			return img;
		}

		protected void saveToCache(int tex, int set, Bitmap img) {
			boolean success = false;

			try {
				FileOutputStream fos = new FileOutputStream(getCachePath(tex, set));
				success = img.compress(Bitmap.CompressFormat.PNG, 90, fos);

				try {
					fos.flush();
					fos.close();
				} catch (Exception innerEx) {
					Common.log(innerEx);
				}
			} catch (Exception ex) {
				Common.log(ex);
			}

			if (!success) {
				final String errorMessage = "Can't save bitmap to cache";
				Common.showToast(errorMessage);
				throw new RuntimeException(errorMessage);
			}
		}

		protected Boolean doInBackground(Void... params) {
			Bitmap img;

			for (int i = 0, lenI = TextureLoader.TEXTURES_TO_LOAD.length; i < lenI; i++) {
				TextureLoader.TextureToLoad texToLoad = TextureLoader.TEXTURES_TO_LOAD[i];

				if (texToLoad.type == TextureLoader.TextureToLoad.TYPE_MAIN) {
					totalCount += mainTexMap.length;
				} else {
					totalCount++;
				}
			}

			for (int i = 0, lenI = TextureLoader.TEXTURES_TO_LOAD.length; i < lenI; i++) {
				TextureLoader.TextureToLoad texToLoad = TextureLoader.TEXTURES_TO_LOAD[i];

				if (texToLoad.type == TextureLoader.TextureToLoad.TYPE_MAIN) {
					for (int j = 0, lenJ = mainTexMap.length; j < lenJ; j++) {
						img = loadMainTexture(mainTexMap[j]);
						saveToCache(texToLoad.tex, j + 1, img);
						img.recycle();
						img = null;

						onImageCached();
					}
				} else {
					if (texToLoad.type == TextureLoader.TextureToLoad.TYPE_MONSTERS_1) {
						img = loadMonTexture(0);
					} else if (texToLoad.type == TextureLoader.TextureToLoad.TYPE_MONSTERS_2) {
						img = loadMonTexture(4);
					} else {
						img = loadAlphaImage(texToLoad.pixelsResId, texToLoad.alphaResId);
					}

					saveToCache(texToLoad.tex, 0, img);
					img.recycle();
					img = null;

					onImageCached();
				}
			}

			return true;
		}

		protected void onImageCached() {
			System.gc();
			cachedCount++;
			publishProgress((int)((float)cachedCount / (float)totalCount * 100.0f));
		}

		protected void onProgressUpdate(Integer... progress) {
			MyApplication.self.getLocalBroadcastManager().sendBroadcast(
				(new Intent(BROADCAST_ACTION))
				.putExtra(EXTRA_PROGRESS, progress[0])
			);
		}

		protected void onPostExecute(Boolean result) {
			onComplete.onComplete();
		}
	}

	public static Bitmap decodeResource(Resources resources, int resId, BitmapFactory.Options bitmapOptions) {
		Bitmap result = null;

		if (bitmapOptions != null) {
			bitmapOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;
		}

		try {
			result = BitmapFactory.decodeResource(resources, resId, bitmapOptions);
		} catch (OutOfMemoryError oom) {
		}

		if (result == null && bitmapOptions != null) {
			bitmapOptions.inPreferredConfig = Bitmap.Config.ARGB_4444;

			try {
				result = BitmapFactory.decodeResource(resources, resId, bitmapOptions);
			} catch (OutOfMemoryError oom) {
			}
		}

		if (result == null) {
			final String oomErrorMessage = "Out of memory while decoding bitmap";

			Common.showToast(oomErrorMessage);
			throw new OutOfMemoryError(oomErrorMessage);
		}

		return result;
	}

	public static int normalizeSetNum(int[][] texMap, int setNum) {
		return ((setNum < 1 || setNum > texMap.length) ? 1 : setNum);
	}

	public static String getCachePath(int tex, int set) {
		StringBuilder sb = new StringBuilder(MyApplication.self.INTERNAL_ROOT);

		sb.append("tex_");
		sb.append(tex);

		if (set != 0) {
			sb.append('_');
			sb.append(set);
		}

		sb.append(".png");
		return sb.toString();
	}

	public static boolean needToUpdateCache() {
		SharedPreferences sp = MyApplication.self.getSharedPreferences();
		MyApplication.self.cachedTexturesReady = false;

		if (sp.getInt("CachedTexturesVersion", 0) < CACHE_VERSION) {
			return true;
		}

		for (int i = 0, lenI = TextureLoader.TEXTURES_TO_LOAD.length; i < lenI; i++) {
			TextureLoader.TextureToLoad texToLoad = TextureLoader.TEXTURES_TO_LOAD[i];

			if (texToLoad.type == TextureLoader.TextureToLoad.TYPE_MAIN) {
				for (int j = 0, lenJ = mainTexMap.length; j < lenJ; j++) {
					if (!(new File(getCachePath(texToLoad.tex, j + 1))).exists()) {
						return true;
					}
				}
			} else if (!(new File(getCachePath(texToLoad.tex, 0))).exists()) {
				return true;
			}
		}

		MyApplication.self.cachedTexturesReady = true;
		return false;
	}

	public static void updateCache() {
		MyApplication.self.handler.post(new Runnable() {
			public void run() {
				if (MyApplication.self.cachedTexturesTask != null) {
					return;
				}

				MyApplication.self.cachedTexturesTask = new Task(new IOnComplete() {
					public void onComplete() {
						SharedPreferences sp = MyApplication.self.getSharedPreferences();
						SharedPreferences.Editor spEdit = sp.edit();
						spEdit.putInt("CachedTexturesVersion", CACHE_VERSION);
						spEdit.commit();

						MyApplication.self.cachedTexturesReady = true;
						MyApplication.self.cachedTexturesTask = null;

						MyApplication.self.getLocalBroadcastManager().sendBroadcast(
							(new Intent(BROADCAST_ACTION))
							.putExtra(EXTRA_PROGRESS, 101)
						);
					}
				});

				MyApplication.self.cachedTexturesTask.execute();
			}
		});
	}
}
