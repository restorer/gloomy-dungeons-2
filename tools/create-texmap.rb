#!/usr/bin/ruby

require 'rubygems'
require 'rmagick'

ROW_TILES = 0 # 5
BASE_WALLS = ROW_TILES * 15
BASE_TRANSP_WALLS = BASE_WALLS + 43
BASE_TRANSP_PASSABLE = BASE_TRANSP_WALLS + 4
BASE_TRANSP_WINDOWS = BASE_TRANSP_PASSABLE + 6
BASE_DOORS_F = BASE_TRANSP_WINDOWS + 8
BASE_DOORS_S = BASE_DOORS_F + 8
BASE_DECOR_ITEM = BASE_DOORS_S + 8
BASE_DECOR_LAMP = BASE_DECOR_ITEM + 10
BASE_FLOOR = BASE_DECOR_LAMP + 1
BASE_CEIL = BASE_FLOOR + 10

ROW_COMMON = 0
BASE_ICONS = ROW_COMMON * 15
BASE_OBJECTS = BASE_ICONS + 10
BASE_BULLETS = BASE_OBJECTS + 21
BASE_EXPLOSIONS = BASE_BULLETS + 4
BASE_ARROWS = BASE_EXPLOSIONS + 3
BASE_BACKS = (ROW_COMMON + 3) * 15

COUNT_MONSTER = 0x10	# block = [up, rt, dn, lt], monster = block[walk_a, walk_b, hit], die[3], shoot

# 225 textures max

class TexMapCreator
	def initialize
		@src_dir = File.dirname(__FILE__) + '/graphics'
		@dst_main_dir = File.dirname(__FILE__) + '/../src/main/res/drawable-nodpi'
		@dst_withhalloween_dir = File.dirname(__FILE__) + '/../src/withhalloween/res/drawable-nodpi'
		@dst_wouthalloween_dir = File.dirname(__FILE__) + '/../src/wouthalloween/res/drawable-nodpi'
	end

	def save_pixels_alpha(img, pixels_name, alpha_name)
		img.channel(Magick::OpacityChannel).negate.write(alpha_name)

		list = Magick::ImageList.new
		list.new_image(img.columns, img.rows) { self.background_color = '#000000' }
		list << img
		list.flatten_images.write(pixels_name) { self.interlace = Magick::PlaneInterlace ; self.quality = 95 }
	end

	def load_alpha_image(name)
		puts name

		img = Magick::ImageList.new(name)
		img.set_channel_depth(Magick::AllChannels, 8)
		img.alpha(Magick::ActivateAlphaChannel)
		img.background_color = '#000000'
		img.alpha(Magick::BackgroundAlphaChannel)

		return img.first
	end

	def load_texture_mon(result, name, tex)
		xpos = (tex % 8) * 128
		ypos = (tex / 8).floor * 128

		img = load_alpha_image(name)
		result.composite!(img, xpos, ypos, Magick::CopyCompositeOp)
	end

	def load_texture(result, name, tex, wrap=:normal)
		xpos = (tex % 15) * 66
		ypos = (tex / 15).floor * 66

		img = load_alpha_image(name)
		result.composite!(img, xpos + 1, ypos + 1, Magick::CopyCompositeOp)

		if wrap == :flip
			result.view(xpos, ypos, 66, 66) do |res_view|
				img.view(0, 0, 64, 64) do |img_view|
					for i in 0 .. 63
						res_view[0][i + 1] = img_view[0][i]
						res_view[i + 1][0] = img_view[i][63]
						res_view[65][i + 1] = img_view[63][i]
						res_view[i + 1][65] = img_view[i][0]
					end

					res_view[0][0] = img_view[0][63]
					res_view[0][65] = img_view[0][0]
					res_view[65][0] = img_view[63][63]
					res_view[65][65] = img_view[63][0]
				end
			end
		elsif wrap == :floor
			result.view(xpos, ypos, 66, 66) do |res_view|
				img.view(0, 0, 64, 64) do |img_view|
					for i in 0 .. 63
						res_view[0][i + 1] = img_view[63][i]
						res_view[i + 1][0] = img_view[i][63]
						res_view[65][i + 1] = img_view[0][i]
						res_view[i + 1][65] = img_view[i][0]
					end

					res_view[0][0] = img_view[63][63]
					res_view[0][65] = img_view[63][0]
					res_view[65][0] = img_view[0][63]
					res_view[65][65] = img_view[0][0]
				end
			end
		elsif wrap == :normal
			result.view(xpos, ypos, 66, 66) do |res_view|
				img.view(0, 0, 64, 64) do |img_view|
					for i in 0 .. 63
						res_view[0][i + 1] = img_view[0][i]
						res_view[i + 1][0] = img_view[i][0]
						res_view[65][i + 1] = img_view[63][i]
						res_view[i + 1][65] = img_view[i][63]
					end

					res_view[0][0] = img_view[0][0]
					res_view[0][65] = img_view[0][63]
					res_view[65][0] = img_view[63][0]
					res_view[65][65] = img_view[63][63]
				end
			end
		end
	end

	def process_common
		base = "#{@src_dir}/common"
		result = Magick::Image.new(1024, 5 * 66).matte_reset!

		load_texture(result, "#{base}/icons/icon_joy.png", BASE_ICONS + 0)
		load_texture(result, "#{base}/icons/icon_menu.png", BASE_ICONS + 1)
		load_texture(result, "#{base}/icons/icon_shoot.png", BASE_ICONS + 2)
		load_texture(result, "#{base}/icons/icon_map.png", BASE_ICONS + 3)
		load_texture(result, "#{base}/icons/icon_health.png", BASE_ICONS + 4)
		load_texture(result, "#{base}/icons/icon_armor.png", BASE_ICONS + 5)
		load_texture(result, "#{base}/icons/icon_ammo.png", BASE_ICONS + 6)
		load_texture(result, "#{base}/icons/icon_blue_key.png", BASE_ICONS + 7)
		load_texture(result, "#{base}/icons/icon_red_key.png", BASE_ICONS + 8)
		load_texture(result, "#{base}/icons/icon_green_key.png", BASE_ICONS + 9)

		load_texture(result, "#{base}/objects/obj_01.png", BASE_OBJECTS + 0)
		load_texture(result, "#{base}/objects/obj_02.png", BASE_OBJECTS + 1)
		load_texture(result, "#{base}/objects/obj_03.png", BASE_OBJECTS + 2)
		load_texture(result, "#{base}/objects/obj_04.png", BASE_OBJECTS + 3)
		load_texture(result, "#{base}/objects/obj_05.png", BASE_OBJECTS + 4)
		load_texture(result, "#{base}/objects/obj_06.png", BASE_OBJECTS + 5)
		load_texture(result, "#{base}/objects/obj_07.png", BASE_OBJECTS + 6)
		load_texture(result, "#{base}/objects/obj_08.png", BASE_OBJECTS + 7)
		load_texture(result, "#{base}/objects/obj_09.png", BASE_OBJECTS + 8)
		load_texture(result, "#{base}/objects/obj_10.png", BASE_OBJECTS + 9)
		load_texture(result, "#{base}/objects/obj_11.png", BASE_OBJECTS + 10)
		load_texture(result, "#{base}/objects/obj_12.png", BASE_OBJECTS + 11)
		load_texture(result, "#{base}/objects/obj_13.png", BASE_OBJECTS + 12)
		load_texture(result, "#{base}/objects/obj_14.png", BASE_OBJECTS + 13)
		load_texture(result, "#{base}/objects/obj_15.png", BASE_OBJECTS + 14)
		load_texture(result, "#{base}/objects/obj_16.png", BASE_OBJECTS + 15)
		load_texture(result, "#{base}/objects/obj_17.png", BASE_OBJECTS + 16)
		load_texture(result, "#{base}/objects/obj_18.png", BASE_OBJECTS + 17)
		load_texture(result, "#{base}/objects/obj_19.png", BASE_OBJECTS + 18)
		load_texture(result, "#{base}/objects/obj_20.png", BASE_OBJECTS + 19)
		load_texture(result, "#{base}/objects/obj_21.png", BASE_OBJECTS + 20)

		load_texture(result, "#{base}/bullets/bull_1_a1.png", BASE_BULLETS + 0)
		load_texture(result, "#{base}/bullets/bull_1_a2.png", BASE_BULLETS + 1)
		load_texture(result, "#{base}/bullets/bull_1_a3.png", BASE_BULLETS + 2)
		load_texture(result, "#{base}/bullets/bull_1_a4.png", BASE_BULLETS + 3)

		load_texture(result, "#{base}/bullets/bull_1_b1.png", BASE_EXPLOSIONS + 0)
		load_texture(result, "#{base}/bullets/bull_1_b2.png", BASE_EXPLOSIONS + 1)
		load_texture(result, "#{base}/bullets/bull_1_b3.png", BASE_EXPLOSIONS + 2)

		load_texture(result, "#{base}/misc/arrow_01.png", BASE_ARROWS + 0)
		load_texture(result, "#{base}/misc/arrow_02.png", BASE_ARROWS + 1)
		load_texture(result, "#{base}/misc/arrow_03.png", BASE_ARROWS + 2)
		load_texture(result, "#{base}/misc/arrow_04.png", BASE_ARROWS + 3)

		load_texture(result, "#{base}/icons/back_joy.png", BASE_BACKS + 0, :nowrap)
		load_texture(result, "#{base}/icons/btn_upgrade.png", BASE_BACKS + 2, :nowrap)

		result.write("#{@dst_main_dir}/texmap_common.png")
	end

	def process(set_idx)
		base = "#{@src_dir}/set-#{set_idx}"
		result = Magick::Image.new(1024, 7 * 66).matte_reset! # 10 * 66 - max

		load_texture(result, "#{base}/walls/wall_01.png", BASE_WALLS + 0, :flip)
		load_texture(result, "#{base}/walls/wall_02.png", BASE_WALLS + 1)
		load_texture(result, "#{base}/walls/wall_03.png", BASE_WALLS + 2)
		load_texture(result, "#{base}/walls/wall_04.png", BASE_WALLS + 3)
		load_texture(result, "#{base}/walls/wall_05.png", BASE_WALLS + 4, :flip)
		load_texture(result, "#{base}/walls/wall_06.png", BASE_WALLS + 5, :flip)
		load_texture(result, "#{base}/walls/wall_07.png", BASE_WALLS + 6, :flip)
		load_texture(result, "#{base}/walls/wall_08.png", BASE_WALLS + 7, :flip)
		load_texture(result, "#{base}/walls/wall_09.png", BASE_WALLS + 8, :flip)
		load_texture(result, "#{base}/walls/wall_10.png", BASE_WALLS + 9, :flip)
		load_texture(result, "#{base}/walls/wall_11.png", BASE_WALLS + 10, :flip)
		load_texture(result, "#{base}/walls/wall_12.png", BASE_WALLS + 11, :flip)
		load_texture(result, "#{base}/walls/wall_13.png", BASE_WALLS + 12)
		load_texture(result, "#{base}/walls/wall_14.png", BASE_WALLS + 13)
		load_texture(result, "#{base}/walls/wall_15.png", BASE_WALLS + 14)
		load_texture(result, "#{base}/walls/wall_16.png", BASE_WALLS + 15)
		load_texture(result, "#{base}/walls/wall_17.png", BASE_WALLS + 16)
		load_texture(result, "#{base}/walls/wall_18.png", BASE_WALLS + 17)
		load_texture(result, "#{base}/walls/wall_19.png", BASE_WALLS + 18)
		load_texture(result, "#{base}/walls/wall_20.png", BASE_WALLS + 19, :flip)
		load_texture(result, "#{base}/walls/wall_21.png", BASE_WALLS + 20, :flip)
		load_texture(result, "#{base}/walls/wall_22.png", BASE_WALLS + 21, :flip)
		load_texture(result, "#{base}/walls/wall_23.png", BASE_WALLS + 22, :flip)
		load_texture(result, "#{base}/walls/wall_24.png", BASE_WALLS + 23, :flip)
		load_texture(result, "#{base}/walls/wall_25.png", BASE_WALLS + 24, :flip)
		load_texture(result, "#{base}/walls/wall_26.png", BASE_WALLS + 25, :flip)
		load_texture(result, "#{base}/walls/wall_27.png", BASE_WALLS + 26, :flip)
		load_texture(result, "#{base}/walls/wall_28.png", BASE_WALLS + 27, :flip)
		load_texture(result, "#{base}/walls/wall_29.png", BASE_WALLS + 28, :flip)
		load_texture(result, "#{base}/walls/wall_30.png", BASE_WALLS + 29, :flip)
		load_texture(result, "#{base}/walls/wall_31.png", BASE_WALLS + 30, :flip)
		load_texture(result, "#{base}/walls/wall_32.png", BASE_WALLS + 31, :flip)
		load_texture(result, "#{base}/walls/wall_33.png", BASE_WALLS + 32, :flip)
		load_texture(result, "#{base}/walls/wall_34.png", BASE_WALLS + 33, :flip)
		load_texture(result, "#{base}/walls/wall_35.png", BASE_WALLS + 34, :flip)
		load_texture(result, "#{base}/walls/wall_36.png", BASE_WALLS + 35)
		load_texture(result, "#{base}/walls/wall_37.png", BASE_WALLS + 36)
		load_texture(result, "#{base}/walls/wall_38.png", BASE_WALLS + 37)
		load_texture(result, "#{base}/walls/wall_39.png", BASE_WALLS + 38)
		load_texture(result, "#{base}/walls/wall_40.png", BASE_WALLS + 39)
		load_texture(result, "#{base}/walls/wall_41.png", BASE_WALLS + 40)
		load_texture(result, "#{base}/walls/wall_42.png", BASE_WALLS + 41)
		load_texture(result, "#{base}/walls/wall_43.png", BASE_WALLS + 42)

		load_texture(result, "#{base}/twall/twall_01.png", BASE_TRANSP_WALLS + 0)
		load_texture(result, "#{base}/twall/twall_02.png", BASE_TRANSP_WALLS + 1, :flip)
		load_texture(result, "#{base}/twall/twall_03.png", BASE_TRANSP_WALLS + 2)
		load_texture(result, "#{base}/twall/twall_04.png", BASE_TRANSP_WALLS + 3, :flip)

		load_texture(result, "#{base}/tpass/tpass_01.png", BASE_TRANSP_PASSABLE + 0, :flip)
		load_texture(result, "#{base}/tpass/tpass_02.png", BASE_TRANSP_PASSABLE + 1)
		load_texture(result, "#{base}/tpass/tpass_03.png", BASE_TRANSP_PASSABLE + 2)
		load_texture(result, "#{base}/tpass/tpass_04.png", BASE_TRANSP_PASSABLE + 3)
		load_texture(result, "#{base}/tpass/tpass_05.png", BASE_TRANSP_PASSABLE + 4)
		load_texture(result, "#{base}/tpass/tpass_06.png", BASE_TRANSP_PASSABLE + 5)

		load_texture(result, "#{base}/twind/twind_01.png", BASE_TRANSP_WINDOWS + 0, :flip)
		load_texture(result, "#{base}/twind/twind_02.png", BASE_TRANSP_WINDOWS + 1, :flip)
		load_texture(result, "#{base}/twind/twind_03.png", BASE_TRANSP_WINDOWS + 2, :flip)
		load_texture(result, "#{base}/twind/twind_04.png", BASE_TRANSP_WINDOWS + 3, :flip)
		load_texture(result, "#{base}/twind/twind_05.png", BASE_TRANSP_WINDOWS + 4, :flip)
		load_texture(result, "#{base}/twind/twind_06.png", BASE_TRANSP_WINDOWS + 5, :flip)
		load_texture(result, "#{base}/twind/twind_07.png", BASE_TRANSP_WINDOWS + 6, :flip)
		load_texture(result, "#{base}/twind/twind_08.png", BASE_TRANSP_WINDOWS + 7, :flip)

		load_texture(result, "#{base}/doors/door_01_f.png", BASE_DOORS_F + 0)
		load_texture(result, "#{base}/doors/door_02_f.png", BASE_DOORS_F + 1)
		load_texture(result, "#{base}/doors/door_03_f.png", BASE_DOORS_F + 2)
		load_texture(result, "#{base}/doors/door_04_f.png", BASE_DOORS_F + 3)
		load_texture(result, "#{base}/doors/door_05_f.png", BASE_DOORS_F + 4)
		load_texture(result, "#{base}/doors/door_06_f.png", BASE_DOORS_F + 5)
		load_texture(result, "#{base}/doors/door_07_f.png", BASE_DOORS_F + 6)
		load_texture(result, "#{base}/doors/door_08_f.png", BASE_DOORS_F + 7)

		load_texture(result, "#{base}/doors/door_01_s.png", BASE_DOORS_S + 0)
		load_texture(result, "#{base}/doors/door_02_s.png", BASE_DOORS_S + 1)
		load_texture(result, "#{base}/doors/door_03_s.png", BASE_DOORS_S + 2)
		load_texture(result, "#{base}/doors/door_04_s.png", BASE_DOORS_S + 3)
		load_texture(result, "#{base}/doors/door_05_s.png", BASE_DOORS_S + 4)
		load_texture(result, "#{base}/doors/door_06_s.png", BASE_DOORS_S + 5)
		load_texture(result, "#{base}/doors/door_07_s.png", BASE_DOORS_S + 6)
		load_texture(result, "#{base}/doors/door_08_s.png", BASE_DOORS_S + 7)

		load_texture(result, "#{base}/ditem/ditem_01.png", BASE_DECOR_ITEM + 0)
		load_texture(result, "#{base}/ditem/ditem_02.png", BASE_DECOR_ITEM + 1)
		load_texture(result, "#{base}/ditem/ditem_03.png", BASE_DECOR_ITEM + 2)
		load_texture(result, "#{base}/ditem/ditem_04.png", BASE_DECOR_ITEM + 3)
		load_texture(result, "#{base}/ditem/ditem_05.png", BASE_DECOR_ITEM + 4)
		load_texture(result, "#{base}/ditem/ditem_06.png", BASE_DECOR_ITEM + 5)
		load_texture(result, "#{base}/ditem/ditem_07.png", BASE_DECOR_ITEM + 6)
		load_texture(result, "#{base}/ditem/ditem_08.png", BASE_DECOR_ITEM + 7)
		load_texture(result, "#{base}/ditem/ditem_09.png", BASE_DECOR_ITEM + 8)
		load_texture(result, "#{base}/ditem/ditem_10.png", BASE_DECOR_ITEM + 9)

		load_texture(result, "#{base}/dlamp/dlamp_01.png", BASE_DECOR_LAMP + 0)

		load_texture(result, "#{base}/floor/floor_01.png", BASE_FLOOR + 0, :floor)
		load_texture(result, "#{base}/floor/floor_02.png", BASE_FLOOR + 1, :floor)
		load_texture(result, "#{base}/floor/floor_03.png", BASE_FLOOR + 2, :floor)
		load_texture(result, "#{base}/floor/floor_04.png", BASE_FLOOR + 3, :floor)
		load_texture(result, "#{base}/floor/floor_05.png", BASE_FLOOR + 4, :floor)
		load_texture(result, "#{base}/floor/floor_06.png", BASE_FLOOR + 5, :floor)
		load_texture(result, "#{base}/floor/floor_07.png", BASE_FLOOR + 6, :floor)
		load_texture(result, "#{base}/floor/floor_08.png", BASE_FLOOR + 7, :floor)
		load_texture(result, "#{base}/floor/floor_09.png", BASE_FLOOR + 8, :floor)
		load_texture(result, "#{base}/floor/floor_10.png", BASE_FLOOR + 9, :floor)

		load_texture(result, "#{base}/ceil/ceil_01.png", BASE_CEIL + 0, :floor)
		load_texture(result, "#{base}/ceil/ceil_02.png", BASE_CEIL + 1, :floor)
		load_texture(result, "#{base}/ceil/ceil_03.png", BASE_CEIL + 2, :floor)
		load_texture(result, "#{base}/ceil/ceil_04.png", BASE_CEIL + 3, :floor)
		load_texture(result, "#{base}/ceil/ceil_05.png", BASE_CEIL + 4, :floor)
		load_texture(result, "#{base}/ceil/ceil_06.png", BASE_CEIL + 5, :floor)

		result.write("#{@dst_main_dir}/texmap_#{set_idx}.png")
	end

	def process_mon(mon_idx, subset)
		if subset == :normal
			subset_dir = 'normal'
			dst_dir = @dst_wouthalloween_dir
		elsif subset == :halloween
			subset_dir = 'halloween'
			dst_dir = @dst_withhalloween_dir
		elsif
			puts "Unknown subset"
		end

		base = "#{@src_dir}/common"
		result = Magick::Image.new(1024, 256).matte_reset!

		load_texture_mon(result, "#{base}/monsters-#{subset_dir}/mon_0#{mon_idx}_a1.png", 0)
		load_texture_mon(result, "#{base}/monsters-#{subset_dir}/mon_0#{mon_idx}_a2.png", 1)
		load_texture_mon(result, "#{base}/monsters-#{subset_dir}/mon_0#{mon_idx}_a3.png", 2)
		load_texture_mon(result, "#{base}/monsters-#{subset_dir}/mon_0#{mon_idx}_a4.png", 3)
		load_texture_mon(result, "#{base}/monsters-#{subset_dir}/mon_0#{mon_idx}_b1.png", 4)
		load_texture_mon(result, "#{base}/monsters-#{subset_dir}/mon_0#{mon_idx}_b2.png", 5)
		load_texture_mon(result, "#{base}/monsters-#{subset_dir}/mon_0#{mon_idx}_b3.png", 6)
		load_texture_mon(result, "#{base}/monsters-#{subset_dir}/mon_0#{mon_idx}_b4.png", 7)
		load_texture_mon(result, "#{base}/monsters-#{subset_dir}/mon_0#{mon_idx}_c1.png", 8)
		load_texture_mon(result, "#{base}/monsters-#{subset_dir}/mon_0#{mon_idx}_c2.png", 9)
		load_texture_mon(result, "#{base}/monsters-#{subset_dir}/mon_0#{mon_idx}_c3.png", 10)
		load_texture_mon(result, "#{base}/monsters-#{subset_dir}/mon_0#{mon_idx}_c4.png", 11)
		load_texture_mon(result, "#{base}/monsters-#{subset_dir}/mon_0#{mon_idx}_d1.png", 12)
		load_texture_mon(result, "#{base}/monsters-#{subset_dir}/mon_0#{mon_idx}_d2.png", 13)
		load_texture_mon(result, "#{base}/monsters-#{subset_dir}/mon_0#{mon_idx}_d3.png", 14)
		load_texture_mon(result, "#{base}/monsters-#{subset_dir}/mon_0#{mon_idx}_e.png",  15)

		save_pixels_alpha(result, "#{dst_dir}/texmap_mon_#{mon_idx}_p.jpg", "#{dst_dir}/texmap_mon_#{mon_idx}_a.png")
		# result.write("#{dst_dir}/texmap_mon_#{mon_idx}.png")
	end

	def process_hit
		base = "#{@src_dir}/common/hit"

		Dir.open(base).each do |name|
			next unless name =~ /\.png$/i

			img = load_alpha_image("#{base}/#{name}")

			subname = name.gsub(/\.png$/i, '')
			save_pixels_alpha(img, "#{@dst_main_dir}/#{subname}_p.jpg", "#{@dst_main_dir}/#{subname}_a.png")
		end
	end

	def optimize
		`pushd "#{@dst_main_dir}" ; optipng -strip all -o7 hit_*.png ; popd`
		`pushd "#{@dst_main_dir}" ; optipng -strip all -o7 texmap_*.png ; popd`
	end
end

tmc = TexMapCreator.new
tmc.process_common
(1 .. 5).each { |idx| tmc.process(idx) }
(1 .. 8).each { |idx| tmc.process_mon(idx, :normal) }
(1 .. 8).each { |idx| tmc.process_mon(idx, :halloween) }
tmc.process_hit
tmc.optimize
