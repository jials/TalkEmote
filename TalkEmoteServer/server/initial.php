<?php

namespace server;

$rootFile = $_SERVER ['DOCUMENT_ROOT'];

require ($rootFile . "/server/Autoloader.php");

use server\Autoloader;

$autoloader = new Autoloader ();

error_reporting ( E_ALL );
session_start ();

?>