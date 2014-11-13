<?php

defined('POPLR') or die();

require_once(POPLR_SOURCES . '/base.core.php');
require_once(POPLR_SOURCES . '/base.db.php');
require_once(POPLR_SOURCES . '/base.user.php');

class Poplr_Context {
	public $settings = array();
	public $siteName = '';
	public $httpRoot = '';
	public $theme = '';
	public $language = '';

	public $themePath = '';
	public $themeUrl = '';
	public $defaultThemePath = '';
	public $defaultThemeUrl = '';

	public $texts = array();
	public $db = null;
	public $modules = array();
	public $actions = array();
	public $actionName = '';
	public $user = null;
	public $availActions = array(); // '<action>' => array( ['user' => REQ_PERMISSION] )
	public $beforeDefaults = array();
	public $beforeRender = array();

	public $defaults = true;
	public $render = true;
	public $renderErrorMessage = '';
	public $title = '';
	public $metas = array();
	public $styles = array();
	public $scripts = array('head' => array(), 'foot' => array());

	public function __construct($settings) {
		$this->settings = $settings;
		$this->siteName = Poplr_Core::defaultize($settings, 'siteName', 'Poplr');
		$this->httpRoot = Poplr_Core::defaultize($settings, 'httpRoot', '');
		$this->theme = Poplr_Core::defaultize($settings, 'theme', 'default');
		$this->language = Poplr_Core::defaultize($settings, 'language', 'default');

		$this->themePath = POPLR_PUBLIC . '/themes/' . $this->theme;
		$this->themeUrl = $this->httpRoot . '/themes/' . $this->theme;
		$this->defaultThemePath = POPLR_PUBLIC . '/themes/default';
		$this->defaultThemeUrl = $this->httpRoot . '/themes/default';

		mb_internal_encoding('UTF-8');
	}

	public function run() {
		$this->loadAppCore();
		$this->loadLanguages();

		$this->db = new Poplr_Db($this, Poplr_Core::defaultize($this->settings, 'db', array()));
		$this->user = new Poplr_User();
		$this->title = $this->siteName;

		$this->loadModulesAndAction();

		if ($this->defaults) {
			$this->hook($this->beforeDefaults);
			$this->styles[] = $this->getThemedUrl('css/stylesheet.css');
		}

		if ($this->render) {
			$this->hook($this->beforeRender);

			if ($this->render) {
				$this->template('layout.index');
			}
		}
	}

	public function sendHeader($name, $value) {
		header("{$name}: {$value}");
	}

	public function sendRedirectHeader($location) {
		$this->sendHeader('Location', $location);
		echo ' '; // fix for some browsers
	}

	public function actionUrl($action=null, $params=array()) {
		if ($action === true) {
			$params = $params + $_GET;
		}

		if ($action === null || $action === false || $action === true) {
			$action = $this->actionName;
		}

		if (!count($params) && $action == 'index') {
			return ($this->httpRoot == '' ? '/' : $this->httpRoot);
		}

		$params = array('action' => $action) + $params;
		$list = array();

		foreach ($params as $key => $value) {
			$list[] = rawurlencode($key) . '=' . rawurlencode($value);
		}

		return $this->httpRoot . '/index.php?' . join('&', $list);
	}

	public function template($name, $args=array()) {
		$__path__ = $this->getThemedPath($name . '.php');

		if (!is_readable($__path__)) {
			$message = "Template {$name} not found.";

			if ($name == 'core.error') {
				echo $message . "\n" . (array_key_exists('message', $args) ? $args['message'] : '');
			} else {
				$this->template('core.error', array(
					'message' => $message,
				));
			}

			return;
		}

		extract($args);
		$poplr = $this;

		if (array_key_exists($this->actionName, $this->actions)) {
			$poplrAction = $this->actions[$this->actionName];
		} else {
			$poplrAction = new stdclass();
		}

		require $__path__;
	}

	public function contentTemplate() {
		if ($this->renderErrorMessage != '') {
			$this->template('core.error', array(
				'message' => $this->renderErrorMessage,
			));

			return;
		}

		if (array_key_exists($this->actionName, $this->availActions)) {
			$actionItem = $this->availActions[$this->actionName];

			if (isset($actionItem['user']) && $actionItem['user'] && !$this->user->can($actionItem['user'])) {
				$this->template('core.error', array(
					'message' => (
						$this->user->can(Poplr_User::PERM_LOGGED) ?
						$this->txt('core.noPermissions') :
						$this->txt('core.loginRequired')
					),
				));
			} else {
				$this->template('action.' . $this->actionName);
			}
		} else {
			$this->template('core.error', array(
				'message' => $this->txt('core.unknownAction'),
			));
		}
	}

	public function etxt() {
		return htmlspecialchars(call_user_func_array(array($this, 'txt'), func_get_args()));
	}

	public function txt() {
		$argc = func_num_args();
		$argv = func_get_args();

		if (!$argc) {
			return '';
		}

		if (is_array($argv[0])) {
			$key = $argv[0][0];
			$number = $argv[0][1];
		} else {
			$key = $argv[0];
			$number = false;
		}

		if (!array_key_exists($key, $this->texts)) {
			if ($argc > 1) {
				return $key . '|' . join('|', array_slice($argv, 1));
			} else {
				return $key;
			}
		}

		if ($number !== false && is_array($this->texts[$key])) {
			// TODO: Need improvements for languages other than english
			$text = ($number == 1 ? $this->texts[$key][0] : $this->texts[$key][1]);
		} else {
			$text = $this->texts[$key];
		}

		if ($argc == 1) {
			return $text;
		}

		return call_user_func_array(
			'sprintf',
			array_merge(
				array($text),
				array_slice($argv, 1)
			)
		);
	}

	public function fatalError() {
		$errorMessage = call_user_func_array(array($this, 'txt'), func_get_args());

		if ($errorMessage == '') {
			$errorMessage = $this->txt('core.fatalError');
		}

		if (PHP_SAPI == 'cli') {
			echo "----\n(E) {$errorMessage}\n";
		} else {
			echo '<html><head><title>' . strip_tags($errorMessage) . '</title></head>'
				. '<body style="padding:0;margin:0;font-family:Tahoma,Arial;font-size:1em;color:#000;background-color:#CCC;">'
				. '<div style="font-size:2em;margin:2em;border:2px solid #F88;background-color:#FFF;padding:2em;text-align:center;color:#F00;'
				. '-moz-border-radius:0.5em;-webkit-border-radius:0.5em;-o-border-radius:0.5em;border-radius:0.5em;">'
				. $errorMessage . '</div></body></html>';
		}

		die();
	}

	public function getThemedPath($subPath) {
		return (is_readable("{$this->themePath}/{$subPath}") ? "{$this->themePath}/{$subPath}" : "{$this->defaultThemePath}/{$subPath}");
	}

	public function getThemedUrl($subPath) {
		return (is_readable("{$this->themePath}/{$subPath}") ? "{$this->themeUrl}/{$subPath}" : "{$this->defaultThemeUrl}/{$subPath}");
	}

	public function fromGet($key, $default='') {
		return (array_key_exists($key, $_GET) ? $_GET[$key] : $default);
	}

	public function fromPost($key, $default='') {
		return (array_key_exists($key, $_POST) ? $_POST[$key] : $default);
	}

	public function fromRequest($key, $default='') {
		return (array_key_exists($key, $_REQUEST) ? $_REQUEST[$key] : $default);
	}

	public function fromSession($key, $default='') {
		return (array_key_exists($key, $_SESSION) ? $_SESSION[$key] : $default);
	}

	public function fromCookie($key, $default='') {
		return (array_key_exists($key, $_COOKIE) ? $_COOKIE[$key] : $default);
	}

	public function sendCookie($name, $value, $period) {
		setcookie($name, $value, time() + $period);
	}

	protected function loadAppCore() {
		if (is_readable(POPLR_SOURCES . '/app.core.php')) {
			$poplr = $this;
			require(POPLR_SOURCES . '/app.core.php');
		}
	}

	protected function loadLanguages() {
		foreach (Poplr_Core::listPaths(POPLR_PUBLIC . '/themes', Poplr_Core::LIST_DIRS) as $themePath) {
			if (is_readable($themePath . '/language.default.php')) {
				$this->texts = (require($themePath . '/language.default.php')) + $this->texts;
			}

			if ($this->language != 'default' && is_readable("{$themePath}/language.{$this->language}.php")) {
				$this->texts = (require("{$themePath}/language.{$this->language}.php")) + $this->texts;
			}
		}
	}

	protected function loadModulesAndAction() {
		$this->actionName = $this->fromGet('action');

		foreach (Poplr_Core::listPaths(POPLR_SOURCES . '/modules', Poplr_Core::LIST_DIRS) as $modulePath) {
			if (is_readable($modulePath . '/module.php')) {
				$poplr = $this;
				require($modulePath . '/module.php');
			}
		}

		if (!array_key_exists($this->actionName, $this->availActions)) {
			$this->actionName = 'index';
		}

		if (is_readable(POPLR_SOURCES . '/action.' . $this->actionName . '.php')) {
			$poplr = $this;
			require(POPLR_SOURCES . '/action.' . $this->actionName . '.php');
		}
	}

	protected function hook($list) {
		foreach ($list as $callback) {
			call_user_func($callback, $this);
		}
	}
}
