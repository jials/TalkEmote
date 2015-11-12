<?php

namespace data;

$rootFile = $_SERVER ['DOCUMENT_ROOT'];
class UploadedFile {
	private $_filename;
	private $_filetype;
	private $_filesize;
	private $_filetemp;
	private $_fileerror;
	private $_filepath;
	private $_isSave;
	const DIRECTORY_UPLOAD = "/";
	const MAX_FILE_SIZE = 1024000;
	const DOT = ".";
	
	public function __construct($filename, $filetype, $filesize, $filetemp, $fileerror) {
		$this->_filename = "test.wav";
		$this->_filetype = $filetype;
		$this->_filesize = $filesize;
		$this->_filetemp = $filetemp;
		$this->_fileerror = $fileerror;
		$this->_isSave = false;
		$this->_filepath = self::createFilePath ( $filename );
	}
	
	public function saveToDirectory() {
		$maxFileSize = self::MAX_FILE_SIZE;
		if ($this->_filesize > $maxFileSize) {
			$response ["success"] = 0;
			$response ["message"] = "File size too big! File size should be at most {$maxFileSize}/1024 kB";
		} elseif ($this->_fileerror > 0) {
			$response ["success"] = 0;
			$response ["message"] = "Error: " . $this->_fileerror . "<br>";
		} elseif (move_uploaded_file ( $this->_filetemp, $this->_filepath )) {
			$this->_isSave = true;
			$response ["success"] = 1;
			$response ["message"] = "success";
		} else {
			$response ["success"] = 0;
			$response ["message"] = "fail";
		}
		return $response;
	}
	
	public function hasError() {
		return $this->_isSave;
	}
	
	public function getFileName() {
		return $this->_filename;
	}
	
	public function getFilePath() {
		if ($this->_isSave == false) {
			return NULL;
		}
		
		return $this->_filepath;
	}
	
	private function createFilePath($filename) {
		global $rootFile;
		
		$file_path = self::DIRECTORY_UPLOAD;
		$file_path = $rootFile . $file_path . basename ( $filename );
		
		if (file_exists ( $file_path )) {
			$file_path = self::getUniqueFilePath ( $file_path );
		}
		
		return $file_path;
	}
	
	private function getUniqueFilePath($file_path) {
		$dot = self::DOT;
		$rand = 0;
		$file_temp_path = $file_path;
		
		$fileDots = explode ( $dot, $file_path );
		$dotsNum = count ( $fileDots ) - 1;
		$fileExtension = $dot . $fileDots [$dotsNum];
		
		while ( file_exists ( $file_temp_path ) ) {
			$file_temp_path = str_replace ( $fileExtension, $rand . $fileExtension, $file_path );
			$rand ++;
		}
		$file_path = $file_temp_path;
		return $file_path;
	}
}

?>