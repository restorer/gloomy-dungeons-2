<?php defined('POPLR') or die() ?>
<meta charset="utf-8" />
<meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1" />
<title><?php echo htmlspecialchars($poplr->title) ?></title>
<meta name="description" content="<?php echo htmlspecialchars($poplr->title) ?>" />
<meta name="author" content="ZameDev" />
<?php foreach ($poplr->metas as $meta) {
	echo '<meta';

	foreach ($meta as $k => $v) {
		echo ' ' . htmlspecialchars($k) . '="' . htmlspecialchars($v) . '"';
	}

	echo " />\n";
} ?>
<link rel="icon" href="<?php echo $poplr->httpRoot ?>/favicon.ico" type="image/x-icon" />
<link rel="shortcut icon" href="<?php echo $poplr->httpRoot ?>/favicon.ico" type="image/x-icon" />
<?php foreach ($poplr->styles as $url) : ?>
	<link rel="stylesheet" type="text/css" href="<?php echo htmlspecialchars($url) ?>" />
<?php endforeach ?>
<?php foreach ($poplr->scripts['head'] as $url) : ?>
	<script type="text/javascript" src="<?php echo htmlspecialchars($url) ?>"></script>
<?php endforeach ?>
