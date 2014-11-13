<?php

defined('POPLR') or die();

class Poplr_ActionUserinfo {
	public $poplr = null;
	public $isValid = false;
	public $name = '';
	public $exp = 0;
	public $achievements = array();

	public function __construct($poplr) {
		$this->poplr = $poplr;
		$this->process();
	}

	public function process() {
		$uid = $this->poplr->fromGet('uid');

		if ($uid == '') {
			return;
		}

		$row = $this->poplr->db->queryRow("SELECT `name`,`exp`,`achievements`,`cheater` FROM `scores` WHERE `uid`=:uid", array(
			':uid' => $uid,
		));

		if ($row != null) {
			$this->isValid = true;
			$this->name = $row['name'];
			$this->exp = ($row['cheater'] ? 1337 : $row['exp']);

			if (!$row['cheater']) {
				foreach (explode('-', $row['achievements']) as $idx) {
					$idxm = $idx - 1;

					$this->achievements[] = array(
						'title' => $this->poplr->txt("achievements.title.{$idxm}"),
						'description' => $this->poplr->txt("achievements.description.{$idxm}"),
					);
				}
			}
		}
	}
}

$poplr->actions['userinfo'] = new Poplr_ActionUserinfo($poplr);
