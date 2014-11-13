<?php

defined('POPLR') or die();

class Poplr_AppCore {
	public $poplr = null;

	public $languages = array(
		'en' => 'default',
		'ru' => 'russian',
	);

	public function __construct($poplr) {
		$this->poplr = $poplr;
		$this->process();
	}

	public function process() {
		$this->poplr->availActions['index'] = array();
		$this->poplr->availActions['help'] = array();
		$this->poplr->availActions['privacy'] = array();
		$this->poplr->availActions['userinfo'] = array();
		$this->poplr->availActions['dislike'] = array();
		$this->poplr->availActions['api'] = array();

		$this->poplr->beforeDefaults[] = array($this, 'beforeDefaults');

		if (!$this->setLanguage($this->poplr->fromGet('hl'))) {
			$this->setLanguage($this->poplr->fromCookie('hl'));
		}
	}

	public function beforeDefaults() {
		$this->poplr->styles[] = 'http://fonts.googleapis.com/css?family=Play:400,700&subset=latin,cyrillic';
		$this->poplr->scripts['head'][] = $this->poplr->getThemedUrl('js/modernizr.js');
		$this->poplr->scripts['foot'][] = $this->poplr->getThemedUrl('js/plugins.js');
		$this->poplr->scripts['foot'][] = $this->poplr->getThemedUrl('js/script.js');
	}

	public function setLanguage($languageKey) {
		if (!array_key_exists($languageKey, $this->languages)) {
			return false;
		}

		$this->poplr->language = $this->languages[$languageKey];
		$this->poplr->sendCookie('hl', $languageKey, 60*60*24*30);
		return true;
	}
}

$poplr->modules['core'] = new Poplr_AppCore($poplr);
