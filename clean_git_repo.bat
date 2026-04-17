@echo off
echo =====================================
echo  Pulizia repository Git (locale)
echo =====================================

REM -------------------------------------
REM Rimuove i file tracciati che ora sono ignorati
REM -------------------------------------
echo.
echo [1/3] Rimozione file Eclipse, Maven e script locali...

git rm --cached .classpath 2>nul
git rm --cached .project 2>nul
git rm --cached -r .settings 2>nul
git rm --cached -r target 2>nul
git rm --cached clean_git_repo.bat 2>nul

REM -------------------------------------
REM Aggiunge il nuovo .gitignore
REM -------------------------------------
echo.
echo [2/3] Aggiunta del file .gitignore...

git add .gitignore

REM -------------------------------------
REM Commit delle modifiche
REM -------------------------------------
echo.
echo [3/3] Commit delle modifiche...

git commit -m "Pulizia repo: rimossi file ignorati (Eclipse, target, script locali), aggiornato .gitignore"

REM -------------------------------------
REM Messaggio finale
REM -------------------------------------
echo.
echo =====================================
echo Pulizia completata!
echo Ora puoi eseguire:
echo     git push
echo per inviare le modifiche al repository remoto.
echo =====================================
pause
