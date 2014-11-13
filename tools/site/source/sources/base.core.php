<?php

defined('POPLR') or die();

class Poplr_Core {
	const LIST_FILES = 1;
	const LIST_DIRS = 2;

	public static function defaultIndexPhpContent() {
		return '<' . '?' . "php\n\n"
			. "if (file_exists(dirname(__FILE__) . '/../index.php')) {\n"
			. "\trequire(dirname(__FILE__) . '/../index.php');\n"
			. "}\n";
	}

	public static function defaultize($list, $key, $default) {
		return (array_key_exists($key, $list) ? $list[$key] : $default);
	}

	public static function listPaths($path, $type, $match='') {
		$result = array();
		$dh = opendir($path);

		if ($dh) {
			while (($name = readdir($dh)) !== false) {
				if ($name != '.' && $name != '..') {
					$resPath = $path . '/' . $name;

					if ((
							(($type & self::LIST_FILES) && is_file($resPath)) ||
							(($type & self::LIST_DIRS) && is_dir($resPath))
						) && ($match == '' || preg_match($match, $name))
					) {
						$result[] = $resPath;
					}
				}
			}

			closedir($dh);
		}

		return $result;
	}

	public static function elipsize($str, $len, $stopAnywhere=false) {
		if (mb_strlen($str) > $len) {
			$str = mb_substr($str, 0, $len - 3);

			if (!$stopAnywhere) {
				$str = mb_substr($str, 0, mb_strrpos($st, ' '));
			}

			$str .= '...';
		}

		return $str;
	}

	public static function now() {
		return date('Y-m-d H:i:s');
	}
}
