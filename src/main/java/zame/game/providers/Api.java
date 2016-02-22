package zame.game.providers;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Locale;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.util.EntityUtils;
import zame.game.BuildConfig;
import zame.game.Common;
import zame.game.MyApplication;

public class Api {
	public static final String GET_PARAMS_SEPARATOR = "&";
	public static final String API_URL = "http://mobile.zame-dev.org/gloomy-ii/index.php?action=api&method=";
	public static final String USERINFO_URL = "http://mobile.zame-dev.org/gloomy-ii/index.php?action=userinfo&uid=%1$s&lang=%2$s";
	public static final String DLC_URL = "http://mobile.zame-dev.org/gloomy-ii/dlc/";

	public static final int STATUS_CODE_OK = 200;
	public static final int STATUS_CODE_NO_CONNECTION = -100;
	public static final int STATUS_CODE_UNKNOWN_ERROR = -101;
	public static final int STATUS_CODE_API_ERROR = -102;
	public static final int STATUS_CODE_WRITE_ERROR = -103;

	protected static final int MAX_RETRIES = 5;

	public interface IDownloadManager {
		boolean isCancelled();
		void onDownloadStarted();
		void onTotalSizeRetrieved(long totalSize);
		void onPartDownloaded(long partSize);
	}

	public static class HttpStatusCode {
		protected int statusCode;

		public HttpStatusCode(int statusCode) {
			this.statusCode = statusCode;
		}

		public int getStatusCode() {
			return statusCode;
		}
	}

	public static String getUserinfoUrl() {
		return String.format(
			Locale.US,
			USERINFO_URL,
			MyApplication.self.profile.playerUid,
			Locale.getDefault().getLanguage().toLowerCase()
		);
	}

	public static Object version() {
		JSONObject request = new JSONObject();
		request.put("packageName", MyApplication.self.getPackageName());

		return sendApiRequest("version", request);
	}

	protected static String calculateSignature(String uid, int exp, String name, String achievements) {
		return Common.md5(Common.md5(String.format(
			Locale.US,
			"%s:%s:%s:%s:B7037764CB0843C1A1E58649658FA2C3",
			uid, String.valueOf(exp), name, achievements
		)));
	}

	public static Object update(String uid, int exp, String name, String achievements) {
		JSONObject request = new JSONObject();
		request.put("uid", uid);
		request.put("exp", exp);
		request.put("name", name);
		request.put("achievements", achievements);
		request.put("sig", calculateSignature(uid, exp, name, achievements));

		return sendApiRequest("update", request);
	}

	public static Object leaderboard(String uid, int exp, String name, String achievements) {
		JSONObject request = new JSONObject();
		request.put("uid", uid);
		request.put("exp", exp);
		request.put("name", name);
		request.put("achievements", achievements);
		request.put("sig", calculateSignature(uid, exp, name, achievements));

		return sendApiRequest("leaderboard", request);
	}

	public static Object dlcTotalSize(List<String> filesList) {
		JSONObject request = new JSONObject();
		request.put("files", filesList);

		return sendApiRequest("dlcTotalSize", request);
	}

	public static Object sendApiRequest(String method, JSONObject value) {
		Object result = sendRequest(method, value);

		if (result instanceof HttpStatusCode) {
			return result;
		}

		String errorCode = Common.asString(result, "error");

		if (errorCode.length() != 0) {
			return new HttpStatusCode(STATUS_CODE_API_ERROR);
		}

		return result;
	}

	protected static DefaultHttpClient getHttpClient() {
		SchemeRegistry registry = new SchemeRegistry();
		registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
		registry.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));

		HttpParams httpParams = new BasicHttpParams();
		HttpProtocolParams.setVersion(httpParams, HttpVersion.HTTP_1_1);
		HttpConnectionParams.setConnectionTimeout(httpParams, 6000);
		HttpConnectionParams.setSoTimeout(httpParams, 5000);

		return new DefaultHttpClient(new ThreadSafeClientConnManager(httpParams, registry), httpParams);
	}

	public static Object sendRequest(String method, JSONObject value) {
		StringBuilder urlSb = new StringBuilder(API_URL);
		urlSb.append(method);

		Object result = new HttpStatusCode(STATUS_CODE_UNKNOWN_ERROR);

		for (int retries = 0; retries < MAX_RETRIES; retries++) {
			try {
				if (BuildConfig.DEBUG) {
					Common.log("sendRequest:request url=[" + urlSb.toString() + "], value=[" + (value == null ? "NULL" : value.toString()) + "]");
				}

				HttpClient httpClient = getHttpClient();
				HttpPost httpPost = new HttpPost(urlSb.toString());

				httpPost.setEntity(new StringEntity(value == null ? "" : value.toString(), "UTF-8"));
				httpPost.addHeader("Content-type", "application/json");

				HttpResponse resp = httpClient.execute(httpPost);
				int statusCode = resp.getStatusLine().getStatusCode();

				String respStr = EntityUtils.toString(resp.getEntity());

				if (BuildConfig.DEBUG) {
					Common.log("sendRequest:response statusCode=[" + String.valueOf(statusCode) + "], resp=[" + respStr + "]");
				}

				if (statusCode == 200) {
					result = JSONValue.parse(respStr);
				} else {
					result = new HttpStatusCode(statusCode);
				}
			} catch (UnknownHostException ex) {
				Common.log(ex.toString());
				result = new HttpStatusCode(STATUS_CODE_NO_CONNECTION);
			} catch (SocketException ex) { // SocketException includes HttpHostConnectException
				Common.log(ex.toString());
				result = new HttpStatusCode(STATUS_CODE_NO_CONNECTION);
			} catch (SocketTimeoutException ex) {
				Common.log(ex.toString());
				result = new HttpStatusCode(STATUS_CODE_NO_CONNECTION);
			} catch (ConnectTimeoutException ex) {
				Common.log(ex.toString());
				result = new HttpStatusCode(STATUS_CODE_NO_CONNECTION);
			} catch (ClientProtocolException ex) {
				Common.log(ex.toString());
				result = new HttpStatusCode(STATUS_CODE_NO_CONNECTION);
			} catch (Exception ex) {
				Common.log(ex);
				result = new HttpStatusCode(STATUS_CODE_UNKNOWN_ERROR);
			}

			if (!(result instanceof HttpStatusCode)) {
				break;
			}

			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
			}
		}

		return result;
	}

	public static int downloadFile(String path, String dlcName, IDownloadManager downloadManager) {
		StringBuilder urlSb = new StringBuilder(DLC_URL);
		urlSb.append(dlcName);

		int result = STATUS_CODE_UNKNOWN_ERROR;
		byte[] buffer = new byte[1024];

		for (int retries = 0; retries < MAX_RETRIES; retries++) {
			if (downloadManager.isCancelled()) {
				result = STATUS_CODE_UNKNOWN_ERROR;
				break;
			}

			downloadManager.onDownloadStarted();

			try {
				HttpClient httpClient = getHttpClient();
				HttpGet httpRequest = new HttpGet(urlSb.toString());
				File cachedFile = new File(path);

				if (cachedFile.exists()) {
					httpRequest.addHeader("Range", "bytes=" + cachedFile.length() + "-");

					if (BuildConfig.DEBUG) {
						Common.log("downloadFile:request url=[" + urlSb.toString() + "], bytes=[" + cachedFile.length() + "-]");
					}
				} else if (BuildConfig.DEBUG) {
					Common.log("downloadFile:request url=[" + urlSb.toString() + "]");
				}

				HttpResponse resp = httpClient.execute(httpRequest);
				result = resp.getStatusLine().getStatusCode();

				for (;;) {
					if (result != 200 && result != 206) {
						if (BuildConfig.DEBUG) {
							Common.log("Api.downloadFile:error statusCode=["
								+ result
								+ "], reason=["
								+ resp.getStatusLine().getReasonPhrase()
								+ "]"
							);
						}

						httpRequest.abort();
						result = STATUS_CODE_UNKNOWN_ERROR;
						break;
					}

					long downloadedSize = 0;
					long totalSize = 0L;
					Header[] headers = resp.getHeaders("Content-Range");

					if (headers.length != 0) {
						if (BuildConfig.DEBUG) {
							Common.log("downloadFile:resp contentRange=[" + headers[0].getValue() + "]");
						}

						String connectionRangesString = headers[0].getValue();
						final int stringOffset = 6; // "bytes=".length() == 6

						if (connectionRangesString.length() > stringOffset) {
							String[] connectionRanges = connectionRangesString.substring(stringOffset).split("-");
							downloadedSize = Long.valueOf(connectionRanges[0]);

							if (connectionRanges.length > 1) {
								String[] endRanges = connectionRanges[1].split("/");

								if (endRanges.length > 1) {
									totalSize = Long.valueOf(endRanges[1]);

									if (totalSize > 0) {
										if (BuildConfig.DEBUG) {
											Common.log("Api.downloadFile:resp totalSize=[" + String.valueOf(totalSize) + "]");
										}
									}
								}
							}
						}
					}

					if (downloadedSize == 0 && cachedFile.exists()) {
						cachedFile.delete();
					}

					if (BuildConfig.DEBUG) {
						Common.log("downloadFile:resp downloadedSize=[" + downloadedSize + "]");
					}

					long contentLength = resp.getEntity().getContentLength();

					if (BuildConfig.DEBUG) {
						Common.log("downloadFile:resp contentLength=[" + contentLength + "]");
					}

					if (contentLength <= 0) {
						httpRequest.abort();
						result = STATUS_CODE_UNKNOWN_ERROR;
						break;
					}

					if (totalSize <= 0L) {
						totalSize = contentLength;
					}

					if (cachedFile.exists() && cachedFile.length() >= totalSize) {
						cachedFile.delete();
						httpRequest.abort();
						result = STATUS_CODE_UNKNOWN_ERROR;
						break;
					}

					downloadManager.onTotalSizeRetrieved(totalSize);
					RandomAccessFile os = new RandomAccessFile(cachedFile, "rw");

					try {
						os.seek(downloadedSize);
					} catch (IOException ex) {
						Common.log(ex.toString());
						httpRequest.abort();
						result = STATUS_CODE_WRITE_ERROR;
						break;
					}

					boolean success = true;
					downloadManager.onPartDownloaded(downloadedSize);

					for (;;) {
						InputStream is;

						try {
							is = resp.getEntity().getContent();
						} catch (IOException ex) {
							Common.log(ex.toString());
							httpRequest.abort();
							result = STATUS_CODE_UNKNOWN_ERROR;
							success = false;
							break;
						}

						long partSize = 0;

						if (BuildConfig.DEBUG) {
							Common.log("Api.downloadFile:downloading");
						}

						for (;;) {
							int len;

							try {
								len = is.read(buffer);
							} catch (IOException ex) {
								Common.log(ex.toString());
								httpRequest.abort();
								result = STATUS_CODE_UNKNOWN_ERROR;
								success = false;
								break;
							}

							if (len <= 0) {
								break;
							}

							if (downloadManager.isCancelled()) {
								if (BuildConfig.DEBUG) {
									Common.log("downloadFile:downloading cancelled");
								}

								break;
							}

							try {
								os.write(buffer, 0, len);
							} catch (Exception ex) {
								Common.log(ex.toString());
								httpRequest.abort();
								result = STATUS_CODE_WRITE_ERROR;
								success = false;
								break;
							}

							partSize += len;
							downloadManager.onPartDownloaded(len);
						}

						if (!success) {
							break;
						}

						if (downloadManager.isCancelled()) {
							httpRequest.abort();
							result = STATUS_CODE_UNKNOWN_ERROR;
							// do not touch "success" veriable, because it is normal situation

							if (BuildConfig.DEBUG) {
								Common.log("downloadFile:after cancelled");
							}

							break;
						}

						try {
							is.close();
						} catch (IOException ex) {
							Common.log(ex.toString());
							// ignore exception
						}

						if (partSize < contentLength) {
							if (BuildConfig.DEBUG) {
								Common.log("Api.downloadFile:after wrong partSize="
									+ partSize
									+ ", contentLength="
									+ contentLength
								);
							}

							result = STATUS_CODE_UNKNOWN_ERROR;
							success = false;
							break;
						}

						if (BuildConfig.DEBUG) {
							Common.log("Api.downloadFile:after success partSize="
								+ partSize
								+ ", contentLength="
								+ contentLength
							);
						}

						result = STATUS_CODE_OK;
						break;
					}

					try {
						os.close();
					} catch (IOException ex) {
						Common.log(ex.toString());

						if (success) {
							result = STATUS_CODE_WRITE_ERROR;
						}
					}

					if (result == STATUS_CODE_WRITE_ERROR) {
						// it can be "No space left on device". delete this file,
						// because many android apps works unpredictable
						// when there is no free space
						cachedFile.delete();
					}

					break;
				}
			} catch (UnknownHostException ex) {
				Common.log(ex.toString());
				result = STATUS_CODE_UNKNOWN_ERROR;
			} catch (SocketException ex) { // SocketException includes HttpHostConnectException
				Common.log(ex.toString());
				result = STATUS_CODE_UNKNOWN_ERROR;
			} catch (SocketTimeoutException ex) {
				Common.log(ex.toString());
				result = STATUS_CODE_UNKNOWN_ERROR;
			} catch (ConnectTimeoutException ex) {
				Common.log(ex.toString());
				result = STATUS_CODE_UNKNOWN_ERROR;
			} catch (ClientProtocolException ex) {
				Common.log(ex.toString());
				result = STATUS_CODE_UNKNOWN_ERROR;
			} catch (RuntimeException ex) {
				Common.log(ex.toString());
				result = STATUS_CODE_UNKNOWN_ERROR;
			} catch (Exception ex) {
				Common.log(ex);
				result = STATUS_CODE_UNKNOWN_ERROR;
			}

			if (BuildConfig.DEBUG) {
				Common.log("downloadFile:preexit result=[" + result + "]");
			}

			if (result == STATUS_CODE_OK) {
				break;
			}

			if (downloadManager.isCancelled()) {
				result = STATUS_CODE_UNKNOWN_ERROR;
				break;
			}

			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
			}
		}

		return result;
	}
}
