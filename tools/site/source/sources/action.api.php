<?php

defined('POPLR') or die();

class Poplr_ActionApi {
	const API_KEY = "B7037764CB0843C1A1E58649658FA2C3";

	// почему не сравниваю с экспой между уровнями? потому что игрок вполне себе может играть без интернета, а потом
	// зайти в интернет и оказаться читером
	const MAX_EXP_TOTAL = 273050;
	const MAX_EXP_BEFORE_CHEATER = 1365250; // MAX_EXP_TOTAL * 5

	public $poplr = null;
	protected $method = '';
	protected $data = null;

	public function __construct($poplr) {
		$this->poplr = $poplr;
		$this->process();
	}

	protected function getValue($key, $def='') {
		if (!is_array($this->data) || !array_key_exists($key, $this->data)) {
			return $def;
		} else {
			return $this->data[$key];
		}
	}

	protected function output($data) {
		header('Content-type: application/json');
		echo json_encode($data);
	}

	protected function actionVersion() {
		if ($this->getValue('packageName') == 'org.zamedev.gloomydungeons2.fullnfree') {
			return array(
				'versionCode' => 1403051421,
				'downloadUrl' => '',
			);
		}

		return array(
			'versionCode' => 0,
			'downloadUrl' => '',
		);
	}

	protected function actionUpdate() {
		$uid = $this->getValue('uid');
		$exp = $this->getValue('exp');
		$name = $this->getValue('name');
		$achievements = $this->getValue('achievements');
		$sig = $this->getValue('sig');

		if (strtoupper($sig) != strtoupper(md5(md5("{$uid}:{$exp}:{$name}:{$achievements}:" . self::API_KEY)))) {
			return array(
				'error' => 'InvalidSignature',
			);
		}

		$toLog = array(
			'time' => date('Y-m-d H:i:s'),
			'uid' => $uid,
			'exp' => $exp,
			'name' => $name,
			'achievements' => $achievements,
		);

		$achievements = join('-', array_filter(array_map(create_function('$v', 'return intval($v);'), explode('-', $achievements))));
		$cheater = false;

		if (($exp < 0) || (($exp % 5) != 0)) {
			$cheater = true;
		}

		if ($uid !== '') {
			$row = $this->poplr->db->queryRow("SELECT `id`,`exp`,`name`,`achievements`,`cheater`,`log` FROM `scores` WHERE `uid`=:uid LIMIT 1", array(
				':uid' => $uid,
			));

			if ($row == null) {
				$uid = '';
			} else if ($row['exp'] != $exp || $row['name'] != $name || $row['achievements'] != $achievements) {
				if ($row['cheater'] || ($exp - $row['exp'] > self::MAX_EXP_TOTAL) || $exp > self::MAX_EXP_BEFORE_CHEATER) {
					$cheater = true;
				}

				$toLog['cheater'] = $cheater;

				$this->poplr->db->execute(
					"UPDATE `scores` SET `exp`=:exp,`name`=:name,`achievements`=:achievements,`cheater`=:cheater,`log`=:log WHERE `id`=:id LIMIT 1",
					array(
						':exp' => $exp,
						':name' => $name,
						':achievements' => $achievements,
						':cheater' => ($cheater ? 1 : 0),
						':log' => ($row['log'] == '' ? '' : $row['log'] . "\n") . json_encode($toLog),
						':id' => $row['id'],
					)
				);
			}
		}

		if ($uid === '' && ($exp > 0 || $achievements !== '' || $name !== '')) {
			if ($exp > self::MAX_EXP_TOTAL) {
				$cheater = true;
			}

			$toLog['cheater'] = $cheater;

			$id = $this->poplr->db->insert(
				"INSERT INTO `scores` (`exp`,`achievements`,`cheater`,`log`) VALUES (:exp,:achievements,:cheater,:log)",
				array(
					':exp' => $exp,
					':achievements' => $achievements,
					':cheater' => ($cheater ? 1 : 0),
					':log' => json_encode($toLog),
				)
			);

			$uid = md5(microtime(true) . mt_rand()) . $id;

			if ($name === '') {
				$name = "Player #{$id}";
			}

			$this->poplr->db->execute("UPDATE `scores` SET `uid`=:uid, `name`=:name WHERE `id`=:id LIMIT 1", array(
				':uid' => $uid,
				':name' => $name,
				':id' => $id,
			));
		}

		return array(
			'uid' => $uid,
			'name' => $name,
		);
	}

	protected function actionLeaderboard() {
		$result = $this->actionUpdate();
		$result['leaderboard'] = array();

		$rows = $this->poplr->db->queryAll("SELECT
			`uid`,`exp`,`name`,`achievements`
			FROM `scores`
			WHERE `id`<240000 AND `cheater`=0 AND NOT (`name` LIKE 'Player #%')
			ORDER BY `exp`
			DESC LIMIT 99
		");

		$result['leaderboard'][] = array(
			'uid' => '',
			'exp' => 0,
			'name' => 'LEADERBOARD DOES NOT ACCEPTING NEW PLAYERS ANYMORE',
			'achievementsCount' => 0,
		);

		foreach ($rows as $row) {
			$result['leaderboard'][] = array(
				'uid' => $row['uid'],
				'exp' => $row['exp'],
				'name' => $row['name'],
				'achievementsCount' => (substr_count($row['achievements'], '-') + 1),
			);
		}

		return $result;
	}

	protected function actionDlcTotalSize() {
		$filesList = $this->getValue('files', array());
		$total = 0;

		foreach ($filesList as $name) {
			if (preg_match('/^[a-z0-9_\-]+\.[a-z0-9_\-]+$/', $name)) {
				if (is_readable(POPLR_PUBLIC . "/dlc/{$name}")) {
					$total += filesize(POPLR_PUBLIC . "/dlc/{$name}");
				}
			}
		}

		return array(
			'total' => $total,
		);
	}

	public function process() {
		$this->poplr->render = false;
		$this->method = $this->poplr->fromGet('method');
		$this->data = json_decode(file_get_contents('php://input'), true);

		if ($this->method === '') {
			$this->output(array(
				'error' => 'NoMethodGiven',
			));
		} else if (!method_exists($this, "action{$this->method}")) {
			$this->output(array(
				'error' => 'UnknownMethod',
				'method' => $this->method,
			));
		} else {
			$result = call_user_func(array($this, "action{$this->method}"));
			$this->output($result);
		}
	}
}

$poplr->actions['api'] = new Poplr_ActionApi($poplr);
