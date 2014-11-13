#!/usr/bin/php
<?php

ini_set('memory_limit', '512M');

function sendRequest($url, $user, $pass, $data=null) {
	$ch = curl_init($url);

	curl_setopt_array($ch, array(
		CURLOPT_RETURNTRANSFER => true,
		CURLOPT_SSL_VERIFYPEER => false,
		CURLOPT_FOLLOWLOCATION => true,
	));

	if ($user != '' && $pass != '') {
		curl_setopt_array($ch, array(
			CURLOPT_USERPWD => "{$user}:{$pass}",
			CURLOPT_HTTPAUTH => CURLAUTH_ANY,
		));
	}

	if ($data) {
		curl_setopt_array($ch, array(
			CURLOPT_POST => true,
			CURLOPT_POSTFIELDS => json_encode($data),
		));
	}

	$resp = curl_exec($ch);
	$status = curl_getinfo($ch);

	if ($status['http_code'] != 200) {
		echo "Error fetching {$url}\n";
		exit();
	}

	if (!$data) {
		return $resp;
	}

	$result = json_decode($resp, true);

	if (!is_array($result)) {
		echo "Json decode error at {$url}, server response:\n\n{$resp}\n\n";
		exit();
	}

	if ($result['error']) {
		echo "Server error at {$url}: {$result['error']}\n";
		exit();
	}

	return $result;
}

function updateLevels($baseUrl, $user, $pass, $resultDir) {
	echo "Fetching...\n";
	$resp = sendRequest($baseUrl, $user, $pass, array('mode' => 'export'));

	foreach ($resp['data'] as $levelName => $levelData) {
		echo "Saving {$levelName}...\n";
		file_put_contents("{$resultDir}/{$levelName}.gd2l", json_encode($levelData));
	}

	echo "Finished\n";
}

updateLevels(
	'PUT_MAPPER_URL_HERE',
	'PUT_AUTH_USER_HERE',
	'PUT_AUTH_PASSWORD_HERE',
	dirname(__FILE__) . 'mapper/levels'
);
