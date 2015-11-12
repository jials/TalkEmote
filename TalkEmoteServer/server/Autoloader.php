<?php
namespace server;

$rootFile = $_SERVER ['DOCUMENT_ROOT'];
class Autoloader {
	public function __construct() {
		spl_autoload_register ( array (
				$this,
				'loader' 
		) );
	}
	
	private function loader($className) {
		global $rootFile;
		
		$className = str_replace ( "\\", "/", $className );
		$className = $rootFile . "/" . $className . '.php';

		if (file_exists ( $className )) {
			require_once ($className);
		}
	}
}
?>