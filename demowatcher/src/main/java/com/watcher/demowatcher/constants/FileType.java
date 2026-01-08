package com.watcher.demowatcher.constants;

public enum FileType {
	
	CSV,
	PDF,
	EXCEL,
	IMAGE,
	WORD,
	TEXT,
	UNKNOWN;
	
	public static FileType fromFileName(String fileName) {
		String extension = getFileExtensiion(fileName);

		return switch (extension) {
		case "csv" -> CSV;
		case "pdf" -> PDF;
		case "xls", "xlsx" -> EXCEL;
		case "jpg", "jpeg", "png", "gif", "bmp" -> IMAGE;
		case "doc", "docx" -> WORD;
		case "txt" -> TEXT;
		default -> UNKNOWN;
		};
	}

	private static String getFileExtensiion(String fileName)
	{
		int lastIndexOf = fileName.lastIndexOf(".");
		if(lastIndexOf == -1)
		{
			return "";
		}
		return fileName.substring(lastIndexOf + 1);
	}

}
