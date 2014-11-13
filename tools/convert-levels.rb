#!/usr/bin/ruby

require 'rubygems'
require 'fileutils'
require 'natcmp'
require 'json'

T_HERO = 1
T_WALL = 2
T_TWALL = 3
T_TPASS = 4
T_TWIND = 5
T_DOOR = 6
T_DITEM = 7
T_DLAMP = 8
T_OBJ = 9
T_MON = 10
T_BWALL = 100

ACTION_CLOSE = 1
ACTION_OPEN = 2
ACTION_REQ_KEY = 3
ACTION_SWITCH = 4
ACTION_NEXT_LEVEL = 5
ACTION_RESTORE_HEALTH = 6
ACTION_SECRET = 7
ACTION_UNMARK = 8
ACTION_ENSURE_WEAPON = 9
ACTION_MSG_ON = 10
ACTION_MSG_OFF = 11
ACTION_FLOOR = 12
ACTION_HELP_ON = 13
ACTION_HELP_OFF = 14
ACTION_CEIL = 15
ACTION_TIMEOUT = 16
ACTION_MSG_FLASH = 17
ACTION_LOOK = 18
ACTION_HELP_ON_ROTATE = 19
ACTION_WALL = 20

WEAPONS_MAP = {
	'pistol' => 1,
	'shotgun' => 2,
	'chaingun' => 3,
	'dblshotgun' => 4,
	'rlauncher' => 5,
	'dblchaingun' => 6,
	'chainsaw' => 7,
}

MESSAGES_MAP = {
	'door' => 18,
	'switch' => 19,
	'turnleft' => 20,
	'turnright' => 21,
	'thisiswindow' => 22,
}

SWITCHES_MAP = {
	20 => { :sw => 21, :exit => false },
	21 => { :sw => 20, :exit => false },

	23 => { :sw => 24, :exit => true },
	24 => { :sw => 23, :exit => true },

	27 => { :sw => 28, :exit => false },
	28 => { :sw => 27, :exit => false },

	32 => { :sw => 33, :exit => false },
	33 => { :sw => 32, :exit => false },

	30 => { :sw => 31, :exit => false },
	31 => { :sw => 30, :exit => false },

	34 => { :sw => 35, :exit => false },
	35 => { :sw => 34, :exit => false },
}

OBJ_BKEY = 3
OBJ_RKEY = 4
OBJ_GKEY = 13

ENSURE = [
	'pistol',
	'shotgun',
	'chaingun',
	'dblshotgun',
	'rlauncher',
	'dblchaingun',
	'chainsaw',
]

MONSTERS = [{
	# Soldier
	:health => 4,
	:hits => 4,
	:weapon => 1
}, {
	# Officer
	:health => 8,
	:hits => 8,
	:weapon => 2
}, {
	# Underground Snake | Worm
	:health => 32,
	:hits => 32,
	:weapon => 0
}, {
	# Incubus | Diablo
	:health => 64,
	:hits => 64,
	:weapon => 0
}, {
	# Robotic Spider
	:health => 12,
	:hits => 12,
	:weapon => 1
}, {
	# Shotyfly | Flying turret
	:health => 24,
	:hits => 24,
	:weapon => 2
}, {
	# ...
	:health => 24,
	:hits => 24,
	:weapon => 0
}, {
	# ...
	:health => 40,
	:hits => 40,
	:weapon => 3
}];

MAX_STATS = {
	:doors => 0,
	:objects => 0,
	:monsters => 0,
	:secrets => 0
}

TOTAL_STATS = {
	:doors => 0,
	:objects => 0,
	:monsters => 0,
	:secrets => 0,
	:levels => 0
}

class Solver
	def initialize
		@wave_to_go = [
			[ 0, -1, 2],
			[-1,  0, 2],
			[ 1,  0, 2],
			[ 0,  1, 2],
			[-1, -1, 3],
			[ 1, -1, 3],
			[-1,  1, 3],
			[ 1,  1, 3]
		]

		@keys_map = {
			:key_blue => :door_blue,
			:key_red => :door_red,
			:key_green => :door_green
		}
	end

	def solve(converter, cache_name, to_name, log_name)
		version_l = converter.level['version'] % 0x100
		version_h = converter.level['version'] / 0x100

		if !cache_name.nil? && File.exists?(cache_name)
			cont = File.read(cache_name)

			if cont[0] == version_l && cont[1] == version_h
				puts "Skip solving"
				FileUtils.copy(cache_name, to_name)
				return
			end
		end

		@log = []
		@hero_x = 0
		@hero_y = 0
		@level = []
		@marks_map = []
		@path_map = []

		converter.level['map'].each_with_index do |in_line, ypos|
			line = []
			marks_line = []
			path_line = []

			in_line.each_with_index do |val, xpos|
				res = :empty

				if val['type'] == T_HERO
					@hero_x = xpos
					@hero_y = ypos
				elsif val['type'] == T_WALL
					if SWITCHES_MAP.key?(val['value']) && SWITCHES_MAP[val['value']][:exit]
						res = :exit
					else
						res = :wall
					end
				elsif val['type'] == T_TWALL
					res = :wall
				elsif val['type'] == T_TPASS
					res = :empty
				elsif val['type'] == T_TWIND
					res = :wall
				elsif val['type'] == T_DOOR
					res = :empty # skip doors. closed doors and doors with keys will be added in exec_actions
				elsif val['type'] == T_OBJ
					if val['value'] == OBJ_BKEY
						res = :key_blue
					elsif val['value'] == OBJ_RKEY
						res = :key_red
					elsif val['value'] == OBJ_GKEY
						res = :key_green
					else
						res = :object
					end
				elsif val['type'] == T_DITEM
					res = :wall
				elsif val['type'] == T_DLAMP
					res = :empty
				end

				line << res
				marks_line << nil
				path_line << false
			end

			@level << line
			@marks_map << marks_line
			@path_map << path_line
		end

		@height = @level.size
		@width = @level[0].size
		@avail_marks = {}

		converter.avail_marks.each do |mark_name, marks|
			@avail_marks[mark_name] = marks.clone
		end

		@actions = {}

		converter.actions.each do |mark_name, actions_line|
			@actions[mark_name] = actions_line.clone

			if mark_name != 0 && @avail_marks.key?(mark_name)
				@avail_marks[mark_name].each do |mark|
					@marks_map[mark[:y]][mark[:x]] = mark[:name]
				end
			end
		end

		@exit_found = false
		@path = []

		exec_actions('')
		process

		solved = [version_l, version_h] + (@exit_found ? @path.flatten : [])

		File.open(to_name, 'wb') { |fo| fo << solved.map{ |v| v.chr }.join }
		FileUtils.copy(to_name, cache_name) unless cache_name.nil?

		File.open(log_name, 'wb') { |fo| fo << @log.join("\n") }
	end

	def fail(msg)
		@log << msg
		puts msg
	end

	def log(msg)
		@log << msg
	end

	def process
		@step = 1

		loop do
			wave_prepare

			loop do
				res = wave_go

				if res.is_a?(Array)
					if !wave_finish(res[0], res[1])
						debug(true)
						fail("Solve error #1")
						return
					end

					break
				elsif res == false
					debug(true)
					fail("Exit not found")
					return
				end
			end

			val = @level[@hero_y][@hero_x]
			was_processed = false

			if val == :object
				@level[@hero_y][@hero_x] = :empty
				was_processed = true
			elsif @keys_map.keys.include?(val)
				@level[@hero_y][@hero_x] = :empty
				door_val = @keys_map[val]
				was_processed = true

				@height.times do |ypos|
					@width.times do |xpos|
						@level[ypos][xpos] = :empty if @level[ypos][xpos] == door_val
					end
				end
			end

			unless @marks_map[@hero_y][@hero_x].nil?
				exec_actions(@marks_map[@hero_y][@hero_x])
				@marks_map[@hero_y][@hero_x] = nil	# just for case
				was_processed = true

				if @level[@hero_y][@hero_x] == :wall
					@hero_x = @path[@path.size - 2][0]
					@hero_y = @path[@path.size - 2][1]
				end
			end

			unless was_processed
				debug
				fail("Solve error #3")
				return
			end

			debug
			break if @exit_found

			@step += 1
		end

		log("[ Solved in #{@step} steps ]")
		puts "Solved in #{@step} steps"
	end

	def can_go(x, y, nx, ny)
		return nx==x || ny==y || (@level[ny][x]==:empty && @level[y][nx]==:empty && @marks_map[ny][x].nil? && @marks_map[y][nx].nil?)
	end

	def wave_finish(x, y)
		tx = x
		ty = y
		current_path = []

		loop do
			found = nil
			@path_map[y][x] = true
			current_path << [x, y]

			@wave_to_go.each do |item|
				nx = x + item[0]
				ny = y + item[1]

				next if nx < 0 || ny < 0 || nx >= @width || ny >= @height
				nv = @wave[ny][nx]

				next if nv == 0
				next if !found.nil? && found[:v] < nv
				next unless can_go(x, y, nx, ny)

				found = {
					:x => nx,
					:y => ny,
					:v => nv
				}
			end

			return false if found.nil?

			x = found[:x]
			y = found[:y]
			break if x == @hero_x && y == @hero_y
		end

		@path += current_path.reverse
		@hero_x = tx
		@hero_y = ty
		return true
	end

	def wave_prepare
		@wave = []

		@height.times do
			line = []

			@width.times do
				line << 0
			end

			@wave << line
		end

		@wave[@hero_y][@hero_x] = 1
	end

	def wave_go
		was_set = false
		new_wave = []
		@wave.each { |line| new_wave << line.clone }

		@height.times do |y|
			@width.times do |x|
				val = @wave[y][x]
				next unless val > 0

				@wave_to_go.each do |item|
					nx = x + item[0]
					ny = y + item[1]

					next if nx < 0 || ny < 0 || nx >= @width || ny >= @height

					nv = val + item[2]

					if can_go(x, y, nx, ny) && (new_wave[ny][nx] == 0 || new_wave[ny][nx] > nv)
						if !@marks_map[ny][nx].nil? || @keys_map.keys.include?(@level[ny][nx]) || @level[ny][nx] == :object
							return [nx, ny]
						elsif @level[ny][nx] == :empty
							new_wave[ny][nx] = nv
							was_set = true
						end
					end
				end
			end
		end

		@wave = new_wave
		return was_set
	end

	def exec_actions(idx)
		actions_to_execute = [idx]

		while !actions_to_execute.empty?
			idx = actions_to_execute.shift

			next unless @actions.key?(idx)
			actions_line = @actions.delete(idx)
			pos = 0

			actions_line.each do |action_item|
				command = action_item[:action]
				marks_list = @avail_marks.key?(action_item[:mark_name]) ? @avail_marks[action_item[:mark_name]] : []
				param = action_item[:argument]

				if command == ACTION_CLOSE
					marks_list.each { |mark| @level[mark[:y]][mark[:x]] = :door_closed }
				elsif command == ACTION_OPEN
					marks_list.each { |mark| @level[mark[:y]][mark[:x]] = :empty }
				elsif command == ACTION_REQ_KEY
					marks_list.each do |mark|
						@level[mark[:y]][mark[:x]] = (param == 1 ? :door_blue : (param == 2 ? :door_red : :door_green))
					end
				elsif command == ACTION_WALL
					marks_list.each do |mark|
						@level[mark[:y]][mark[:x]] = (param == 0 ? :empty : :wall)
					end
				elsif command == ACTION_NEXT_LEVEL
					@exit_found = true
				elsif command == ACTION_UNMARK
					marks_list.each { |mark| @marks_map[mark[:y]][mark[:x]] = nil }
				elsif command == ACTION_TIMEOUT
					actions_to_execute << action_item[:mark_name]
				elsif [
					ACTION_RESTORE_HEALTH,
					ACTION_MSG_OFF,
					ACTION_SECRET,
					ACTION_ENSURE_WEAPON,
					ACTION_MSG_ON,
					ACTION_FLOOR,
					ACTION_HELP_ON,
					ACTION_HELP_OFF,
					ACTION_CEIL,
					ACTION_MSG_FLASH,
					ACTION_LOOK,
					ACTION_HELP_ON_ROTATE,
					ACTION_SWITCH,
				].include?(command)
					# Do nothing
				else
					fail("Solver: Unknown action #{action_item[:action]} at idx=#{idx}")
				end
			end
		end
	end

	def debug(print_wave=false)
		log("[ Step #{@step} ]")

		@level.each_with_index do |line, ypos|
			str = ''

			line.each_with_index do |val, xpos|
				has_mark = !@marks_map[ypos][xpos].nil?

				if print_wave && @wave[ypos][xpos] != 0
					str += '%02d' % @wave[ypos][xpos]
				elsif xpos == @hero_x && ypos == @hero_y
					str += '@@'
				elsif @path_map[ypos][xpos]
					str += val == :wall ? '#o' : 'oo'
				elsif val == :exit
					str += has_mark ? 'EE' : 'ee'
				elsif val == :wall
					str += has_mark ? '$$' : '##'
				elsif val == :key_blue
					str += has_mark ? 'b!' : 'bb'
				elsif val == :key_red
					str += has_mark ? 'r!' : 'rr'
				elsif val == :key_green
					str += has_mark ? 'g!' : 'gg'
				elsif val == :door_blue
					str += has_mark ? 'B!' : 'BB'
				elsif val == :door_red
					str += has_mark ? 'R!' : 'RR'
				elsif val == :door_green
					str += has_mark ? 'G!' : 'GG'
				elsif val == :door_closed
					str += has_mark ? 'X!' : 'XX'
				elsif val == :object
					str += has_mark ? '^!' : '^^'
				elsif !@marks_map[ypos][xpos].nil?
					str += '..'
				else
					str += '  '
				end

				str += ' ' if print_wave
			end

			log(str)
		end

		log('')
	end
end

class Converter
	attr_reader :level
	attr_reader :avail_marks
	attr_reader :actions
	attr_reader :has_errors

	def initialize
		@level = {}
		@avail_marks = {}
		@actions = []
		@has_errors = false
	end

	def fill_borders
		for i in 0 ... @hgt
			@map[i][0]['type'] = T_BWALL if @map[i][0]['type'] != T_WALL
			@map[i][@wdt - 1]['type'] = T_BWALL if @map[i][@wdt - 1]['type'] != T_WALL
		end

		for i in 0 ... @wdt
			@map[0][i]['type'] = T_BWALL if @map[0][i]['type'] != T_WALL
			@map[@hgt - 1][i]['type'] = T_BWALL if @map[@hgt - 1][i]['type'] != T_WALL
		end

		loop do
			repeat = false

			for i in 1 ... @hgt - 1
				for j in 1 ... @wdt - 1
					if @map[i][j]['type'] == 0 && (
						@map[i - 1][j]['type'] == T_BWALL || @map[i + 1][j]['type'] == T_BWALL ||
						@map[i][j - 1]['type'] == T_BWALL || @map[i][j + 1]['type'] == T_BWALL
					)
						@map[i][j]['type'] = T_BWALL
						repeat = true
					end
				end
			end

			break unless repeat
		end

		for i in 0 ... @hgt
			for j in 0 ... @wdt
				if @map[i][j]['type'] == T_BWALL
					@map[i][j]['type'] = T_WALL
					@map[i][j]['value'] = 1
				end
			end
		end
	end

	def read_level(from_name)
		@stats = {
			:doors => 0,
			:objects => 0,
			:monsters => 0,
			:secrets => 0,
		}

		@data = []
		@level = JSON.parse(File.read(from_name))

		unless @level['format'] == 2
			puts "Unsupported level format"
			@has_errors = true
		end

		@map = @level['map']
		@hgt = @map.size
		@wdt = @map.first.size

		fill_borders()

		@data << @level['graphics'].gsub('set-', '').to_i

		health_mult = @level['difficultyLevel']
		hits_mult = [1.to_f, @level['difficultyLevel']].min

		MONSTERS.each_with_index do |mon, mon_idx|
			@data << mon_idx + 1
			@data << [1, (mon[:health].to_f * health_mult).to_i].max
			@data << [1, (mon[:hits].to_f * hits_mult).to_i].max
			@data << mon[:weapon]
		end

		@data << 0
		@data << @wdt
		@data << @hgt

		@avail_marks = {}
		@marks = []

		for i in 0 ... @hgt
			for j in 0 ... @wdt
				item = @map[i][j]
				vert = false

				if [T_TWIND, T_DOOR].include?(item['type'])
					ltype = @map[i][j - 1]['type']
					rtype = @map[i][j + 1]['type']
					utype = @map[i - 1][j]['type']
					dtype = @map[i + 1][j]['type']

					if ltype == T_WALL && rtype == T_WALL
						vert = false
					elsif utype == T_WALL && dtype == T_WALL
						vert = true
					else
						vert = [T_WALL, T_TWIND, T_DOOR].include?(utype) && [T_WALL, T_TWIND, T_DOOR].include?(dtype)
					end
				end

				if item['type'] == T_DOOR
					@stats[:doors] += 1
				elsif item['type'] == T_OBJ
					@stats[:objects] += 1
				elsif item['type'] == T_MON
					@stats[:monsters] += 1
					@stats[:objects] += 1
				end

				@data << (item['type'] | (item['noTrans'] ? 128 : 0) | (vert ? 64 : 0))
				@data << item['value']

				if item['floor'].is_a?(Array)
					@data << item['floor'][0]
					@data << item['floor'][1]
					@data << item['floor'][2]
					@data << item['floor'][3]
				else
					@data << 0
					@data << 0
					@data << 0
					@data << 0
				end

				if item['ceil'].is_a?(Array)
					@data << item['ceil'][0]
					@data << item['ceil'][1]
					@data << item['ceil'][2]
					@data << item['ceil'][3]
				else
					@data << 0
					@data << 0
					@data << 0
					@data << 0
				end

				@data << item['arrow'] || 0

				if item['mark'] != ''
					mark_name = item['mark'].downcase
					mark_item = { :name => mark_name, :x => j, :y => i }
					@avail_marks[mark_name] = (@avail_marks[mark_name] || []) << mark_item
					@marks << mark_item
				end
			end
		end
	end

	def try_toggle_switch(actions_line, mark_name)
		return unless @avail_marks.key?(mark_name)
		return if @avail_marks[mark_name].empty?

		mark = @avail_marks[mark_name].first
		item = @map[mark[:y]][mark[:x]]

		return unless item['type'] == T_WALL
		return unless SWITCHES_MAP.key?(item['value'])

		actions_line << {
			:action => ACTION_SWITCH,
			:mark_name => mark_name,
			:argument => SWITCHES_MAP[item['value']][:sw]
		}
	end

	def process_actions
		@used_mark_names = {}
		@used_secrets = []
		no_unmark = {}

		@actions = { '' => [ {
			:action => ACTION_RESTORE_HEALTH
		} ] }

		@level['ensureLevel'].times do |ensure_idx|
			@actions[''] << { :action => ACTION_ENSURE_WEAPON, :argument => WEAPONS_MAP[ENSURE[ensure_idx]] }
		end

		@level['actions'].gsub(/\r/, "\n").gsub(/\n{2,}/, "\n").strip.split("\n").each do |line|
			line = line.strip
			next if line.empty?
			next if line =~ /^#/

			spl = line.split(':').map{ |v| v.strip.downcase }
			mark_name = spl[0]
			commands = spl[1].split(',').map{ |v| v.strip }.reject{ |v| v.empty? }

			@used_mark_names[mark_name] = true unless mark_name.empty?
			do_try_toggle = false

			unless @actions.key?(mark_name)
				@actions[mark_name] = [] unless @actions.key?(mark_name)
				do_try_toggle = true
			end

			actions_line = @actions[mark_name]
			try_toggle_switch(actions_line, mark_name) if do_try_toggle

			commands.each do |cmd_line|
				spl = cmd_line.split(' ').map{ |v| v.strip }.reject{ |v| v.empty? }

				command = spl[0]
				param_1 = spl.size > 1 ? spl[1] : nil
				param_2 = spl.size > 2 ? spl[2] : nil

				if ['bkey', 'rkey', 'gkey', 'close', 'open', 'bury', 'wall', 'floor', 'ceil', 'timeout', 'look'].include?(command)
					if param_1.nil?
						puts "No mark name given for \"#{command}\" in line \"#{line}\""
						param_1 = 'nonexistingmark'
						@has_errors = true
					else
						unless @avail_marks.key?(param_1) || command == 'timeout'
							puts "Mark \"#{param_1}\" not exists (command \"#{command}\" in line \"#{line}\")"
							@has_errors = true
						end

						@used_mark_names[param_1] = true
					end
				end

				if ['secret', 'ensure', 'show'].include?(command) && param_1.nil?
					puts "No argument given for \"#{command}\" in line \"#{line}\""
					param_1 = 'nonexistingargument'
					@has_errors = true
				end

				if command == 'timeout' && param_2.nil?
					puts "No argument given for \"#{command}\" in line \"#{line}\""
					param_2 = '0'
					@has_errors = true
				end

				if ['wall', 'floor', 'ceil'].include?(command)
					if param_2.nil?
						puts "No argument given for \"#{command}\" in line \"#{line}\""
						param_2 = 'nonexistingargument'
						@has_errors = true
					else
						unless @avail_marks.key?(param_2)
							puts "Mark \"#{param_2}\" not exists (command \"#{command}\" in line \"#{line}\")"
							@has_errors = true
						end

						@used_mark_names[param_2] = true
					end
				end

				if command == 'bkey'
					actions_line << { :action => ACTION_REQ_KEY, :mark_name => param_1, :argument => 1 }
				elsif command == 'rkey'
					actions_line << { :action => ACTION_REQ_KEY, :mark_name => param_1, :argument => 2 }
				elsif command == 'gkey'
					actions_line << { :action => ACTION_REQ_KEY, :mark_name => param_1, :argument => 4 }
				elsif command == 'exit'
					actions_line << { :action => ACTION_NEXT_LEVEL }
				elsif command == 'close'
					actions_line << { :action => ACTION_CLOSE, :mark_name => param_1 }
				elsif command == 'open'
					actions_line << { :action => ACTION_OPEN, :mark_name => param_1 }
				elsif command == 'bury'
					actions_line << { :action => ACTION_WALL, :mark_name => param_1, :argument => 0 }
				elsif command == 'burythis'
					actions_line << { :action => ACTION_WALL, :mark_name => mark_name, :argument => 0 }
				elsif command == 'wall'
					actions_line << { :action => ACTION_WALL, :mark_name => param_1, :argument_mark_name => param_2 }
				elsif command == 'secret'
					if @used_secrets.include?(param_1)
						secret_mask = 1 << @used_secrets.index(param_1)
					else
						secret_mask = 1 << @used_secrets.size
						@used_secrets << param_1
						@stats[:secrets] += 1
					end

					actions_line << { :action => ACTION_SECRET, :mark_name => mark_name, :argument => secret_mask }
				elsif command == 'ensure'
					if WEAPONS_MAP.key?(param_1)
						weapon_idx = WEAPONS_MAP[param_1]
					else
						puts "Unknown weapon \"#{param_1}\" (command \"#{command}\" in line \"#{line}\")"
						weapon_idx = 1
						@has_errors = true
					end

					actions_line << { :action => ACTION_SECRET, :argument => weapon_idx }
				elsif command == 'showmessage'
					if MESSAGES_MAP.key?(param_1)
						message_idx = MESSAGES_MAP[param_1]
					else
						puts "Unknown message \"#{param_1}\" (command \"#{command}\" in line \"#{line}\")"
						message_idx = 1
						@has_errors = true
					end

					actions_line << { :action => ACTION_MSG_ON, :argument => message_idx }
				elsif command == 'hidemessage'
					actions_line << { :action => ACTION_MSG_OFF }
				elsif command == 'floor'
					actions_line << { :action => ACTION_FLOOR, :mark_name => param_1, :argument_mark_name => param_2 }
				elsif command == 'showhelp'
					actions_line << { :action => ACTION_HELP_ON }
				elsif command == 'hidehelp'
					actions_line << { :action => ACTION_HELP_OFF }
				elsif command == 'ceil'
					actions_line << { :action => ACTION_CEIL, :mark_name => param_1, :argument_mark_name => param_2 }
				elsif command == 'timeout'
					timeout = param_2.to_i

					if timeout < 1
						puts "Timeout < 1 (command \"#{command}\" in line \"#{line}\")"
						timeout = 1
						@has_errors = true
					end

					actions_line << { :action => ACTION_TIMEOUT, :mark_name => param_1, :argument => timeout }
				elsif command == 'flashmessage'
					if MESSAGES_MAP.key?(param_1)
						message_idx = MESSAGES_MAP[param_1]
					else
						puts "Unknown message \"#{param_1}\" (command \"#{command}\" in line \"#{line}\")"
						message_idx = 1
						@has_errors = true
					end

					actions_line << { :action => ACTION_MSG_FLASH, :argument => message_idx }
				elsif command == 'look'
					actions_line << { :action => ACTION_LOOK, :mark_name => param_1 }
				elsif command == 'nounmark'
					no_unmark[mark_name] = true
				elsif command == 'showhelprotate'
					actions_line << { :action => ACTION_HELP_ON_ROTATE }
				else
					puts "Unknown command \"#{command}\" in line \"#{line}\""
					@has_errors = true
				end
			end
		end

		@actions.each do |mark_name, actions_line|
			next if mark_name.empty?
			next if no_unmark.key?(mark_name) && no_unmark[mark_name]

			actions_line << { :action => ACTION_UNMARK, :mark_name => mark_name }
		end
	end

	def save_result(to_name, actions_log_name)
		current_mark_id = 0
		@mark_ids = { '' => 0 }

		@used_mark_names.keys.sort.each do |mark_name|
			current_mark_id += 1
			@mark_ids[mark_name] = current_mark_id
		end

		unless actions_log_name.nil?
			File.open(actions_log_name, 'wb') do |fo|
				inverted_ids = @mark_ids.invert

				inverted_ids.keys.sort.each do |mark_id|
					fo << "#{'%02d' % mark_id} => #{inverted_ids[mark_id]}".strip + "\n"
				end
			end
		end

		@marks.each do |mark|
			if @used_mark_names.key?(mark[:name])
				@data << @mark_ids[mark[:name]]
				@data << mark[:x]
				@data << mark[:y]
			end
		end

		@data << 255

		@actions.each do |mark_name, actions_line|
			@data << @mark_ids[mark_name]

			actions_line.each do |action_item|
				@data << action_item[:action]
				@data << (action_item[:mark_name].nil? ? 0 : @mark_ids[action_item[:mark_name]])

				if action_item[:argument_mark_name].nil?
					@data << (action_item[:argument].nil? ? 0 : action_item[:argument].to_i)
				else
					@data << @mark_ids[action_item[:argument_mark_name]]
				end
			end

			@data << 0
		end

		@data << 255
		File.open(to_name, 'wb') { |fo| fo << @data.map{ |v| v.chr }.join }
	end

	def convert(from_name, to_name, actions_log_name)
		@has_errors = false

		read_level(from_name)
		process_actions()
		save_result(to_name, actions_log_name)

		MAX_STATS[:doors] = [MAX_STATS[:doors], @stats[:doors]].max
		MAX_STATS[:objects] = [MAX_STATS[:objects], @stats[:objects]].max
		MAX_STATS[:monsters] = [MAX_STATS[:monsters], @stats[:monsters]].max
		MAX_STATS[:secrets] = [MAX_STATS[:secrets], @stats[:secrets]].max

		TOTAL_STATS[:doors] += @stats[:doors]
		TOTAL_STATS[:objects] += @stats[:objects]
		TOTAL_STATS[:monsters] += @stats[:monsters]
		TOTAL_STATS[:secrets] += @stats[:secrets]
		TOTAL_STATS[:levels] += 1

		return @has_errors
	end
end

def process(builder_mode)
	if builder_mode
		log_dir = '/var/www/zame-dev.org/projects/gloomy-ii-mapper/build/logs'
		src_levels_dir = '/var/www/zame-dev.org/projects/gloomy-ii-mapper/levels'
		src_solved_dir = nil
		dest_levels_dir = '/var/www/zame-dev.org/projects/gloomy-ii-mapper/build/.srvbuild/assets/levels'
		dest_solved_dir = '/var/www/zame-dev.org/projects/gloomy-ii-mapper/build/.srvbuild/assets/solved'
	else
		log_dir = File.dirname(__FILE__) + '/../temp/solver-logs'
		src_levels_dir = File.dirname(__FILE__) + '/mapper/levels'
		src_solved_dir = File.dirname(__FILE__) + '/mapper/levels-solved'
		dest_levels_dir = File.dirname(__FILE__) + '/../src/main/assets/levels'
		dest_solved_dir = File.dirname(__FILE__) + '/../src/main/assets/solved'
	end

	FileUtils.mkdir_p(log_dir)
	FileUtils.mkdir_p(src_solved_dir) unless src_solved_dir.nil?
	FileUtils.mkdir_p(dest_levels_dir)
	FileUtils.mkdir_p(dest_solved_dir)

	list = Dir.open(src_levels_dir) .
		reject { |name| !(name =~ /\.gd2l$/) } .
		map { |name| name.gsub(/\.gd2l$/, '') } .
		sort { |a, b| Natcmp::natcmp(a, b) }

	list.each do |name|
		puts "Converting #{name}"
		converter = Converter.new

		converter.convert(
			"#{src_levels_dir}/#{name}.gd2l",
			"#{dest_levels_dir}/#{name}.map",
			builder_mode ? nil : "#{log_dir}/actions.#{name}.log"
		)

		unless converter.has_errors
			Solver.new.solve(
				converter,
				src_solved_dir.nil? ? nil : "#{src_solved_dir}/#{name}.slv",
				"#{dest_solved_dir}/#{name}.slv",
				"#{log_dir}/solver.#{name}.log"
			)
		end
	end

	max_exp_update = MAX_STATS[:doors] * 1 * 5 +
		MAX_STATS[:objects] * 5 * 5 +
		MAX_STATS[:monsters] * 25 * 5 +
		MAX_STATS[:secrets] * 50 * 5 +
		100 * 5

	total_exp_update = TOTAL_STATS[:doors] * 1 * 5 +
		TOTAL_STATS[:objects] * 5 * 5 +
		TOTAL_STATS[:monsters] * 25 * 5 +
		TOTAL_STATS[:secrets] * 50 * 5 +
		TOTAL_STATS[:levels] * 100 * 5

	puts "Doors: #{MAX_STATS[:doors]} max / #{TOTAL_STATS[:doors]} total"
	puts "Objects: #{MAX_STATS[:objects]} max / #{TOTAL_STATS[:objects]} total"
	puts "Monsters: #{MAX_STATS[:monsters]} max / #{TOTAL_STATS[:monsters]} total"
	puts "Secrets: #{MAX_STATS[:secrets]} max / #{TOTAL_STATS[:secrets]} total"
	puts "Exp update: #{max_exp_update} max / #{total_exp_update} total (#{TOTAL_STATS[:levels]} levels)"
end

process(ARGV.first == 'builder')
