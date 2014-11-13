#!/usr/bin/ruby

require 'rubygems'
require 'backports'

class JavaRandom
	def initialize(seed)
		@multiplier = 0x5DEECE66D
		@addend = 0xB
		@mask = (1 << 48) - 1
		@mask_int = 0xFFFFFFFF

		set_seed(seed)
	end

	def set_seed(seed)
		@seed = (seed ^ @multiplier) & @mask
	end

	def rand_next(bits)
		@seed = (@seed * @multiplier + @addend) & @mask
		return (@seed >> (48 - bits)) & @mask_int
	end

	def rand(n)
		# i.e., n is a power of 2
		if (n & -n) == n
			return ((n * rand_next(31)) >> 31) & @mask_int
		end

		val = 0

		loop do
			bits = rand_next(31)
			val = bits % n

			break unless bits - val + (n-1) < 0
		end

		return val
	end
end

class Generator
	def generate(seed, len)
		@random = JavaRandom.new(seed)
		@map_width = 9
		@map_height = 5
		@map = Array.new(@map_height) { Array.new(@map_width, false) }
		@path = generate_path(@map_width / 2, @map_height / 2, len) || []
		optimize_path unless @path.empty?
	end

	def generate_path(x, y, len)
		return nil if x < 0
		return nil if y < 0
		return nil if x >= @map_width
		return nil if y >= @map_height
		return nil if @map[y][x]
		return [ { :x => x, :y => y } ] if len <= 1

		dirs = [
			{ :dx => 0, :dy => -1 },
			{ :dx => 1, :dy => 0 },
			{ :dx => 0, :dy => 1 },
			{ :dx => -1, :dy => 0 },
		]

		already = Array.new(dirs.size, false)
		tries = dirs.size

		loop do
			cnt = @random.rand(tries)
			dir_idx = 0

			while already[dir_idx] || cnt > 0
				cnt -= 1 unless already[dir_idx]
				dir_idx += 1
			end

			already[dir_idx] = true

			@map[y][x] = true
			path = generate_path(x + dirs[dir_idx][:dx], y + dirs[dir_idx][:dy], len - 1)
			@map[y][x] = false

			return [ { :x => x, :y => y } ] + path unless path.nil?

			tries -= 1
			return nil if tries <= 0
		end
	end

	def optimize_path
		min_x = @path.min_by{ |v| v[:x] }[:x]
		min_y = @path.min_by{ |v| v[:y] }[:y]

		@path.map!{ |v| { :x => v[:x] - min_x, :y => v[:y] - min_y } }

		@path_w = @path.max_by{ |v| v[:x] }[:x] + 1
		@path_h = @path.max_by{ |v| v[:y] }[:y] + 1
	end

	def print
		pmap = Array.new(@path_h) { Array.new(@path_w, 0) }

		@path.each_with_index do |v, idx|
			pmap[v[:y]][v[:x]] = idx + 1
		end

		pmap.each do |line|
			puts line.map{ |v| v == 0 ? '  ' : '%2d' % v }.join(' ')
		end
	end
end

gen = Generator.new
gen.generate(0, 10) ; gen.print
