<?php

defined('POPLR') or die();

class Poplr_ActionDislike {
	public $poplr = null;
	public $thankYou = false;

	public function __construct($poplr) {
		$this->poplr = $poplr;
		$this->process();
	}

	public function process() {
		$this->poplr->defaults = false;
		$this->poplr->metas[] = array('name' => 'viewport', 'content' => 'width=device-width');
		$this->poplr->styles[] = $this->poplr->getThemedUrl('css/mobile.css');

		$this->thankYou = ($this->poplr->fromGet('thank') == 'you');

		if ($this->thankYou || !$this->poplr->fromPost('send')) {
			return;
		}

		$response = trim($this->poplr->fromPost('response'));

		if ($response == '') {
			return;
		}

		if ($this->poplr->db->queryRow("SELECT `id` FROM `dislikes` WHERE `response`=:response", array(
			':response' => $response,
		))) {
			$this->thankYou = true;
			return;
		}

		$this->poplr->db->insert("INSERT INTO `dislikes` (`createdAt`,`response`) VALUES (:createdAt,:response)", array(
			':createdAt' => Poplr_Core::now(),
			':response' => $response,
		));

		$this->poplr->sendRedirectHeader($this->poplr->actionUrl(null, array('thank' => 'you')));
	}
}

$poplr->actions['dislike'] = new Poplr_ActionDislike($poplr);
