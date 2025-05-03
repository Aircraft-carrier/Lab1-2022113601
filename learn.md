# R0
git status

# R1
git init
git add .

# R2
git commit -m "Initial commit"

# 手动修改文件后...
# R3
git diff
git commit -am "Fix: updated files"  # -a 直接添加所有已跟踪文件的修改

# 再次修改后...
# R5
git add .
git commit -m "More changes"

# R6 把最后一次提交撤销；
git reset --soft HEAD~1

# R7 查询该项目的全部提交记录；
git log --oneline

# 推送到远程仓库
git branch -M main
git remote add origin git@github.com:Aircraft-carrier/Lab1-2022113601.git
git push -u origin main

# 完善下面的内容
# R1 获得本地Lab1仓库的全部分支，切换至分支master；
git branch
git checkout master

# R2 在master基础上建立两个分支B1、B2；
git branch B1
git branch B2

# R3 在B2分支基础上创建一个新分支C4；
git checkout B2
git branch C4
git checkout C4

# R4 在C4上，对2个文件进行修改并提交；
echo "C4修改" >> file1.txt
git add file1.txt
git commit -m "C4提交"

# R5 在B1分支上对同样的2个文件做不同修改并提交；
git checkout B1
echo "B1修改" >> file1.txt
git add file1.txt
git commit -m "B1提交"

# R6 将C4合并到B1分支，若有冲突，手工消解；
git merge C4
# 解决冲突后...
git add file1.txt
git commit -m "合并C4到B1"

# R7 在B2分支上对2个文件做修改并提交；
git checkout B2
echo "B2修改" >> file1.txt
git commit -am "B2提交"

# R8 查看目前哪些分支已经合并、哪些分支尚未合并；
git branch --merged
git branch --no-merged

# R9 将已经合并的分支删除，将尚未合并的分支合并到一个新分支上，分支名字为你的学号；
git checkout -b 2022113601
git merge B2

# R10 将本地以你的学号命名的分支推送到GitHub上自己的仓库内；
git push origin 2022113601

# R11 查看完整的版本变迁树；
git log --graph --oneline --all

# R12 在Github上以web页面的方式查看你的Lab1仓库的当前状态
# 浏览器打开 GitHub 仓库页面