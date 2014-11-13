<?php

echo '<li class="lang">';
$sep = false;

foreach ($poplr->modules['core']->languages as $k => $v) {
	if ($sep) {
		echo ' | ';
	}

	if ($v == $poplr->language) {
		echo '<strong>' . $k .'</strong>';
	} else {
		echo '<a href="' . $poplr->actionUrl(true, array('hl' => $k)) . '">' . $k . '</a>';
	}

	$sep = true;
}

echo '</li>';
