# AgileSummer2016


## Running the project

To run the project:

Clone the repository from GitHub

Navigate to the ftp-client directory

run "mvn clean" and then "mvn package -DskipTests"

(The unit tests currently work on their own but break the maven build)

To run the client, use "java -jar target/ftp-client-1.0-SNAPSHOT.jar"

## GitHub Workflow
## Branching

To start working on the project:

git clone git@github.com:Hegnell/AgileSummer2016.git

At any time, type "git status" in the directory to get a picture of your current state.

**On branch master
Your branch is up-to-date with 'origin/master'.**

This tells you what branch you are currently on, along with what files are tracker.

Make sure to use "git pull" to ensure you have the latest master branch.

The development branch "dev" is what you should be merging to.

## Basic workflow

When on master branch, type

**git checkout -b branch_name**

This will create a new local branch for you, typing "git status" will show you what branch you are on. The -b flag will create a new local branch if it does not exist already.

A good branch name for a feature might include a brief description of the feature to be worked on, or the name of the ticket currently worked on.

Once you are satisfied with your changes, "git status" will show you the current state of your local directory. Files that have changed and are relevant can be added to the staging area using:

**git add filename**

Once all files of interest have been added, use git commit to add all of the files to the staging area, and use the -m flag to add a message describing the changes. For example:

**git commit -m 'Finished login feature.'**

Once files are added to the staging area, they are ready to be pushed to the remote repository.To push the files to the remote repository, type :

**git push**

If this is a new branch and a remote branch does not yet exist, git will inform you of this issue when you attempt to push. The message displayed will look something like:

> warning: push.default is unset; its implicit value has changed in
> Git 2.0 from 'matching' to 'simple'. To squelch this message
> and maintain the traditional behavior, use:

>   git config --global push.default matching

>   To squelch this message and adopt the new behavior now, use:

>     git config --global push.default simple

>     When push.default is set to 'matching', git will push local branches
>     to the remote branches that already exist with the same name.

>     Since Git 2.0, Git defaults to the more conservative 'simple'
>     behavior, which only pushes the current branch to the corresponding
>     remote branch that 'git pull' uses to update the current branch.

>     See 'git help config' and search for 'push.default' for further information.
>     (the 'simple' mode was introduced in Git 1.7.11. Use the similar mode
>     'current' instead of 'simple' if you sometimes use older versions of Git)

>     fatal: The current branch test has no upstream branch.
>    To push the current branch and set the remote as upstream, use

>    git push --set-upstream origin test


The only thing you need to worry about here is the very last part, which will create a remote branch and push all of your changes to that branch. So, type:

**git push --set-upstream origin test**

Once this is done, your branch will be visible through the GitHub gui. Once this is done, navigate to your branch in the gui and you will see a "Create Pull Request" button (or something similar). Clicking this will let you choose a source branch (your branch) and a target branch (master) to merge into. This will create a request to merge all of the changes that you have made into the target branch (master), and upon approval, GitHub will attempt to merge the two files together. If this is unsuccessful (mostly due to multiple people working on the same part of a file) GitHub will let you know and spit out instructions for you to move along further. This is a part that might involve some googling, but it should be fairly straight forward.

Let me know if you have any questions!
