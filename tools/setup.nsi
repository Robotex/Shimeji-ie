;--------------------------------

;--------------------------------
;Include Modern UI

  !include "MUI2.nsh"

;--------------------------------

!define APPNAME "Shimeji-ie"
!define APPFULLNAME "Shimeji - Italian Enhanced" 

!ifndef VERSION
!define VERSION "Alpha"
!endif

; The name of the installer
Name "${APPFULLNAME} [${VERSION}]"

; The file to write
OutFile "..\target\shimeji-ie_setup.exe"

;Default installation folder
InstallDir "$PROGRAMFILES\${APPNAME}"

; Request application privileges for Windows Vista
RequestExecutionLevel admin

;--------------------------------
;Variables

;--------------------------------
;Interface Settings

  !define MUI_ABORTWARNING
  
;--------------------------------
;Pages

	!insertmacro MUI_PAGE_COMPONENTS
	!insertmacro MUI_PAGE_DIRECTORY
	!insertmacro MUI_PAGE_INSTFILES
  
  !insertmacro MUI_UNPAGE_CONFIRM
  !insertmacro MUI_UNPAGE_INSTFILES
  
;--------------------------------
;Languages
 
	!insertmacro MUI_LANGUAGE "English"
	!insertmacro MUI_LANGUAGE "Italian"

;--------------------------------

; Check Java Runtime's presence
!define JRE_VERSION "1.6"
!define JRE_URL "http://javadl.sun.com/webapps/download/AutoDL?BundleId=52252"
!include "JREDyna_Inetc.nsh"

; The stuff to install
Section "${APPFULLNAME}" SecRequired
	SectionIn RO
	
	; Check for required Java Runtime
	call DownloadAndInstallJREIfNecessary
	
	; Set output path to the installation directory.
	SetOutPath "$INSTDIR"
	
	; Put file there
	File /r "..\target\conf"
	File /r "..\target\lib"
	File /r "..\target\img"
	File "..\target\Shimeji.jar"
	File "..\target\LICENSE.txt"
	File "Shimeji.exe"
	
	;Create uninstaller
	WriteUninstaller "$INSTDIR\Uninstall.exe"
  
	;Create shortcuts
	CreateDirectory "$SMPROGRAMS\${APPFULLNAME}"
	CreateShortCut "$SMPROGRAMS\${APPFULLNAME}\Shimeji.lnk" "$INSTDIR\Shimeji.exe"  "" "" "" "" "" "Shimeji desktop mascot!"
	CreateShortCut "$SMPROGRAMS\${APPFULLNAME}\Uninstall.lnk" "$INSTDIR\Uninstall.exe"
	
SectionEnd ; end the section

; [Optional component] Run on startup
Section "Esegui all'avvio" SecRunOnStartup
	SectionIn 1
	
	CreateShortCut "$SMPROGRAMS\Startup\Shimeji.lnk" "$INSTDIR\Shimeji.exe" "" "" "" "" "" "Shimeji desktop mascot!"
SectionEnd

;--------------------------------
;Descriptions

  ;USE A LANGUAGE STRING IF YOU WANT YOUR DESCRIPTIONS TO BE LANGAUGE SPECIFIC

  ;Assign descriptions to sections
  !insertmacro MUI_FUNCTION_DESCRIPTION_BEGIN
    !insertmacro MUI_DESCRIPTION_TEXT ${SecRequired} "Installa i files necessari."
	!insertmacro MUI_DESCRIPTION_TEXT ${SecRunOnStartup} "Esegue ad ogni accensione del computer."
  !insertmacro MUI_FUNCTION_DESCRIPTION_END
  
;--------------------------------
;Uninstaller Section

Section "Uninstall"

	;ADD YOUR OWN FILES HERE...
	RMDir /r "$INSTDIR\img"
	RMDir /r "$INSTDIR\lib"
	RMDir /r "$INSTDIR\conf"
	Delete "$INSTDIR\Shimeji.jar"
	Delete "$INSTDIR\Shimeji.exe"
	Delete "$INSTDIR\LICENSE.txt"

	Delete "$INSTDIR\Uninstall.exe"

	RMDir "$INSTDIR"
	
	Delete "$SMPROGRAMS\Startup\Shimeji.lnk"
	Delete "$SMPROGRAMS\${APPFULLNAME}\Shimeji.lnk"
	Delete "$SMPROGRAMS\${APPFULLNAME}\Uninstall.lnk"
	RMDir "$SMPROGRAMS\${APPFULLNAME}"
SectionEnd