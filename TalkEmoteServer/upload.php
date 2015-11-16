<?php
namespace server;

set_time_limit(0);


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

#echo "Upload: " . $filename . "<br>";
#echo "Type: " . $filetype . "<br>";
#echo "Size: " . ($filesize / 1024) . " kB <br>";
#echo "Stored in: " . $filetemp . "<br>";
    
$response = $curFile->saveToDirectory();
#echo (json_encode($response));

$response = exec("java -jar EmotionRecognizer-V0.1.jar");
#echo $response;

$responseArray = explode("|", $response);
$responseArrays[0] = explode(":", $responseArray[0]);
$responseArrays[1] = explode(":", $responseArray[1]);

$jsonObject[$responseArrays[0][0]] = $responseArrays[0][1];
$jsonObject[$responseArrays[1][0]] = $responseArrays[1][1];

echo json_encode($jsonObject, JSON_FORCE_OBJECT);


?>