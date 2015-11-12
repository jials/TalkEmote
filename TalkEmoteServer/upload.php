<?php
namespace server;

$rootFile = $_SERVER['DOCUMENT_ROOT'];

require_once ($rootFile . '/server/initial.php');

use data\UploadedFile;

function endswith($string, $test) {
    $strlen = strlen($string);
    $testlen = strlen($test);
    if ($testlen > $strlen) return false;
    return substr_compare($string, $test, $strlen - $testlen, $testlen) === 0;
}
	
$dot = ".";

$filename = $_FILES["uploaded_file"]["name"];
$filetype = $_FILES["uploaded_file"]["type"];
$filesize = $_FILES["uploaded_file"]["size"];
$filetemp = $_FILES["uploaded_file"]["tmp_name"];
$fileerror = $_FILES['uploaded_file']['error'];

if (!endsWith($filename, ".wav")) {
    echo "please upload the correct file format";
    return;
}


$curFile = new UploadedFile($filename, $filetype, $filesize, $filetemp, $fileerror);

echo "Upload: " . $filename . "<br>";
echo "Type: " . $filetype . "<br>";
echo "Size: " . ($filesize / 1024) . " kB <br>";
echo "Stored in: " . $filetemp . "<br>";
    
$response = $curFile->saveToDirectory();
echo (json_encode($response));

?>