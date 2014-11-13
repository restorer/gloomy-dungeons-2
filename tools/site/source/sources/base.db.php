<?php

defined('POPLR') or die();

require_once(POPLR_SOURCES . '/base.core.php');

class Poplr_Db {
	protected $poplr = null;
	protected $dbh = null;

	public $prefix = '';

	public function __construct($poplr, $settings) {
		$this->poplr = $poplr;
		$this->prefix = Poplr_Core::defaultize($settings, 'prefix', '');

		try {
			$this->dbh = new PDO(
				Poplr_Core::defaultize($settings, 'dsn', ''),
				Poplr_Core::defaultize($settings, 'username', ''),
				Poplr_Core::defaultize($settings, 'password', ''),
				array(PDO::MYSQL_ATTR_INIT_COMMAND => "SET NAMES 'UTF8'")
			);
		} catch (PDOException $e) {
			$poplr->fatalError('db.connectionFailed', $e->getMessage());
		}
	}

	public function quote($value) {
		return $this->dbh->quote($value);
	}

	public function queryAll($sql, $params=array()) {
		try {
			$sth = $this->dbh->prepare($sql);

			if (!@$sth->execute($params)) {
				$this->poplr->fatalError('db.sqlError', 'Error in queryAll');
				return;
			}

			$result = $sth->fetchAll(PDO::FETCH_ASSOC);
			$sth->closeCursor();
			return $result;
		} catch (PDOException $e) {
			$this->poplr->fatalError('db.sqlError', $e->getMessage());
		}
	}

	public function queryRow($sql, $params=array()) {
		try {
			$sth = $this->dbh->prepare($sql);

			if (!@$sth->execute($params)) {
				$this->poplr->fatalError('db.sqlError', 'Error in queryRow');
				return;
			}

			$result = $sth->fetch(PDO::FETCH_ASSOC);
			$sth->closeCursor();
			return $result;
		} catch (PDOException $e) {
			$this->poplr->fatalError('db.sqlError', $e->getMessage());
		}
	}

	public function queryOne($sql, $params=array()) {
		try {
			$sth = $this->dbh->prepare($sql);

			if (!@$sth->execute($params)) {
				$this->poplr->fatalError('db.sqlError', 'Error in queryOne');
				return;
			}

			$result = $sth->fetchColumn();
			$sth->closeCursor();
			return $result;
		} catch (PDOException $e) {
			$this->poplr->fatalError('db.sqlError', $e->getMessage());
		}
	}

	public function execute($sql, $params=array()) {
		try {
			$sth = $this->dbh->prepare($sql);

			if (!@$sth->execute($params)) {
				$this->poplr->fatalError('db.sqlError', 'Error in execute');
				return;
			}

			return $sth->rowCount();
		} catch (PDOException $e) {
			$this->poplr->fatalError('db.sqlError', $e->getMessage());
		}
	}

	public function insert($sql, $params=array()) {
		try {
			$sth = $this->dbh->prepare($sql);

			if (!@$sth->execute($params)) {
				$this->poplr->fatalError('db.sqlError', 'Error in insert');
				return;
			}

			return $this->dbh->lastInsertId();
		} catch (PDOException $e) {
			$this->poplr->fatalError('db.sqlError', $e->getMessage());
		}
	}
}
