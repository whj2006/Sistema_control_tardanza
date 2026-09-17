; Inno Setup Script para Control de Retraso
; Requiere Inno Setup 6: https://jrsoftware.org/isinfo.php
; Compila el instalador Windows con JRE embebido si usas jpackage, o con EXE Launch4j

#define MyAppName "Control de Retraso"
#define MyAppVersion "1.0.0"
#define MyAppPublisher "IES Jose Ballester Gozalvo"
#define MyAppURL "https://github.com/whj2006/Sistema_control_tardanza"
#define MyAppExeName "CONTROL-RETRASO.exe"

[Setup]
AppId={{8A1F4B2C-3D7E-4F9A-B2C1-5D6E7F8A9B0C}
AppName={#MyAppName}
AppVersion={#MyAppVersion}
AppPublisher={#MyAppPublisher}
AppPublisherURL={#MyAppURL}
AppSupportURL={#MyAppURL}
AppUpdatesURL={#MyAppURL}
; INSTALACION SIN ADMIN - NO PIDE CONTRASEÑA DE ADMINISTRADOR
PrivilegesRequired=lowest
PrivilegesRequiredOverridesAllowed=dialog
DefaultDirName={localappdata}\Programs\{#MyAppName}
DefaultGroupName={#MyAppName}
AllowNoIcons=yes
LicenseFile=..\README.md
OutputDir=target\installer
OutputBaseFilename=ControlRetraso-Setup-{#MyAppVersion}-Portable
Compression=lzma
SolidCompression=yes
WizardStyle=modern
SetupIconFile=src\main\resources\images\icon.ico
UninstallDisplayIcon={app}\{#MyAppExeName}
; No crear entrada que requiera admin
Uninstallable=yes
CreateUninstallRegKey=yes
; Mensaje que no modifica sistema
InfoBeforeFile=
InfoAfterFile=

[Languages]
Name: "spanish"; MessagesFile: "compiler:Languages\Spanish.isl"
Name: "english"; MessagesFile: "compiler:Default.isl"

[Tasks]
Name: "desktopicon"; Description: "{cm:CreateDesktopIcon}"; GroupDescription: "{cm:AdditionalIcons}"; Flags: unchecked

[Files]
; EXE portable - no requiere admin, se instala en %LOCALAPPDATA%\Programs
Source: "target\CONTROL-RETRASO.exe"; DestDir: "{app}"; Flags: ignoreversion
Source: "target\control-tardanzas-1.0.0-jar-with-dependencies.jar"; DestDir: "{app}"; Flags: ignoreversion
Source: "..\Gestion_retraso_sql\sql_completo.sql"; DestDir: "{app}\sql"; Flags: ignoreversion
Source: "..\Manual_ControlRetraso.docx.pdf"; DestDir: "{app}"; Flags: ignoreversion
; config.properties se crea al primer arranque, no se incluye para evitar sobreescribir
; Si quieres incluir ejemplo:
Source: "..\config.properties.example"; DestDir: "{app}"; DestName: "config.properties.example"; Flags: ignoreversion
; Si usas jpackage, cambia la Source a target\dist\*

[Icons]
Name: "{group}\{#MyAppName}"; Filename: "{app}\{#MyAppExeName}"; IconFilename: "{app}\{#MyAppExeName}"
Name: "{group}\{cm:UninstallProgram,{#MyAppName}}"; Filename: "{uninstallexe}"
Name: "{autodesktop}\{#MyAppName}"; Filename: "{app}\{#MyAppExeName}"; Tasks: desktopicon; IconFilename: "{app}\{#MyAppExeName}"

[Run]
Filename: "{app}\{#MyAppExeName}"; Description: "{cm:LaunchProgram,{#StringChange(MyAppName, '&', '&&')}}"; Flags: nowait postinstall skipifsilent

[Code]
function InitializeSetup(): Boolean;
var
  V: string;
  JavaInstalled: Boolean;
begin
  JavaInstalled := RegQueryStringValue(HKLM, 'SOFTWARE\JavaSoft\Java Runtime Environment', 'CurrentVersion', V);
  if not JavaInstalled then
    JavaInstalled := RegQueryStringValue(HKLM, 'SOFTWARE\Eclipse Adoptium\JDK\17\hotspot\MSI', 'Path', V);
  
  if not JavaInstalled then
  begin
    MsgBox('Este programa necesita Java 17 o superior.' + #13#10 + 
           'Por favor, instala Java desde https://adoptium.net/' + #13#10 +
           'El instalador continuara, pero la aplicacion no funcionara sin Java.', mbInformation, MB_OK);
  end;
  Result := True;
end;
