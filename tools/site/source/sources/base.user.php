<?php

defined('POPLR') or die();

class Poplr_User {
	const PERM_LOGGED = 1;
	const PERM_APPROVE = 2;
	const PERM_ADMIN = 4;

	public $uid = '';
	public $name = '';
	public $token = '';

	public function __construct() {
	}

	public function can($what) {
		return false;
	}
}
