# Before syncing you need to add upstream once using this command:
# remote add upstream https://github.com/litan/kojojs-core.git
echo *** trying to sync with remote upstream
git remote -v
git fetch upstream
git checkout master
git merge upstream/master
