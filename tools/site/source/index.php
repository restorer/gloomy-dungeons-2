<?php

define('POPLR', true);
define('POPLR_PUBLIC', dirname(__FILE__));
define('POPLR_SOURCES', dirname(__FILE__) . '/sources');

require_once(POPLR_SOURCES . '/base.context.php');

$poplr = new Poplr_Context(require_once(dirname(__FILE__) . '/settings.php'));
$poplr->run();
