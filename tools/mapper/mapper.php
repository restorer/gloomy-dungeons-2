<?php

if (isset($_SERVER['REQUEST_METHOD']) && strtoupper($_SERVER['REQUEST_METHOD']) == 'OPTIONS') {
	header('Access-Control-Allow-Origin: *');
	header('Access-Control-Allow-Credentials: true');
	header('Access-Control-Allow-Headers: Content-type, Authorization');
	return;
}

define('BASE_DIR', dirname(__FILE__) . '/levels');

function response($error, $data=null) {
	return json_encode(array(
		'error' => $error,
		'data' => $data,
	));
}

function process() {
	$inputData = @file_get_contents('php://input');

	if ($inputData == '') {
		return response('NO_INPUT_DATA');
	}

	$req = @json_decode($inputData, true);

	if (!is_array($req)) {
		return response('INVALID_JSON');
	}

	if ($req['mode'] == 'list') {
		$res = array();
		$dh = @opendir(BASE_DIR);

		if (!$dh) {
			return response('CANT_OPEN_DIR');
		}

		while (false !== ($name = readdir($dh))) {
			if (!is_dir(BASE_DIR . "/$name") && preg_match('/\.gd2l$/', $name)) {
				$res[] = preg_replace('/\.gd2l$/', '', $name);
			}
		}

		closedir($dh);
		natsort($res);

		return response(false, array_values($res));
	} elseif ($req['mode'] == 'save') {
		$name = preg_replace('/[^a-z0-9\-_.]/i', '', $req['name']);

		if ($name == '') {
			return response('INVALID_NAME');
		}

		if (!is_array($req['data'])) {
			return response('DATA_MISSING');
		}

		$version = 1;
		$level = @file_get_contents(BASE_DIR . "/{$name}.gd2l");

		if ($level != '') {
			$level = @json_decode($level, true);

			if (is_array($level) && isset($level['version'])) {
				$version = $level['version'] + 1;
			}
		}

		$req['data']['version'] = $version;
		$res = @file_put_contents(BASE_DIR . "/{$name}.gd2l", json_encode($req['data']));

		if ($res) {
			@chmod(BASE_DIR . "/{$name}.gd2l", 0666);
			return response(false);
		} else {
			return response('CANT_SAVE_FILE');
		}
	} elseif ($req['mode'] == 'load') {
		$name = preg_replace('/[^a-z0-9\-_.]/i', '', $req['name']);

		if ($name == '') {
			return response('INVALID_NAME');
		}

		$res = @file_get_contents(BASE_DIR . "/{$name}.gd2l");

		if ($res == '') {
			return response('FILE_NOT_FOUND');
		}

		$res = @json_decode($res, true);

		if (!is_array($res)) {
			return response('DECODE_ERROR');
		}

		return response(false, $res);
	} elseif ($req['mode'] == 'export') {
		$res = array();
		$dh = @opendir(BASE_DIR);

		if (!$dh) {
			return response('CANT_OPEN_DIR');
		}

		echo '{"error":false,"data":{';
		$sep = false;

		while (false !== ($name = readdir($dh))) {
			if (!is_dir(BASE_DIR . "/$name") && preg_match('/^e\\d{2}m\\d{2}\\.gd2l$/', $name)) {
				$level = @file_get_contents(BASE_DIR . "/{$name}");

				if ($level != '') {
					$level = @json_decode($level, true);

					if (is_array($level)) {
						if ($sep) {
							echo ',';
						}

						echo '"' . preg_replace('/\.gd2l$/', '', $name) . '":';
						echo json_encode($level);
						$sep = true;
					}
				}
			}
		}

		closedir($dh);

		echo '}}';
		return '';
	}

	return response('INVALID_MODE');
}

echo process();
