#include "..\control-tardanzas\target\installer-version.iss"

#define MyAppName "Control de Tardanzas"
#define MyAppExe "ControlTardanzas.exe"
#define MyAppPublisher "IES Jose Ballester Gozalvo"
#define MyAppURL "https://github.com/whj2006/Sistema_control_tardanza"
#define AppImageDir "..\control-tardanzas\target\windows-package\app-image\ControlTardanzas"

[Setup]
AppId={{B867D8D4-7931-4C69-8FF8-40657D85ED13}
AppName={#MyAppName}
AppVersion={#MyAppVersion}
AppPublisher={#MyAppPublisher}
AppPublisherURL={#MyAppURL}
AppSupportURL={#MyAppURL}/issues
DefaultDirName={localappdata}\Programs\ControlTardanzas
DefaultGroupName={#MyAppName}
DisableProgramGroupPage=yes
PrivilegesRequired=lowest
ArchitecturesInstallIn64BitMode=x64
OutputDir=..\dist
OutputBaseFilename=ControlTardanzas-Setup
Compression=lzma2/ultra64
SolidCompression=yes
WizardStyle=modern
UninstallDisplayIcon={app}\{#MyAppExe}

[Languages]
Name: "spanish"; MessagesFile: "compiler:Languages\Spanish.isl"

[Tasks]
Name: "desktopicon"; Description: "Crear un acceso directo en el escritorio"; \
    GroupDescription: "Accesos directos:"; Flags: unchecked

[Files]
Source: "{#AppImageDir}\*"; DestDir: "{app}"; \
    Flags: ignoreversion recursesubdirs createallsubdirs

[Icons]
Name: "{group}\{#MyAppName}"; Filename: "{app}\{#MyAppExe}"
Name: "{autodesktop}\{#MyAppName}"; Filename: "{app}\{#MyAppExe}"; \
    Tasks: desktopicon

[Run]
Filename: "{app}\{#MyAppExe}"; Description: "Abrir {#MyAppName}"; \
    Flags: postinstall nowait skipifsilent
