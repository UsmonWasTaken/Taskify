# Skip Danger if there is a #skip_danger tag.
if github.pr_body.include?("#skip_danger")
  message "Skipping Danger due to #skip_danger tag"
  return
end

# Make it more obvious that a PR is a work in progress and shouldn't be merged yet.
if github.pr_json["mergeable_state"] == "draft"
  message "Skipping Danger since PR is classed as Draft"
  return
elsif github.pr_title.include? "[WIP]"
  warn "Skipping Danger since PR is classed as WIP"
  return
end

# Thank if the contributor is external.
if github.pr_author != "usdaves"
  message "Thanks @#{github.pr_author}! :tada:"
end

# Check if PR has no or small description.
if github.pr_body.length == 0
  fail "Please provide a summary in the Pull Request description"
elsif github.pr_body.length < 50 && git.lines_of_code > 50
  warn "Please provide a little more detailed summary in the Pull Request description"
end

# Check if PR has no or more than 5 labels.
if github.pr_labels.empty?
  warn "Please add labels to this PR."
elsif github.pr_labels.count > 5
  warn "This PR has more than 5 labels, consider removing some of them"
end

# Warn when there is a big PR
if git.lines_of_code > 500
  warn "Please consider breaking up this PR into smaller ones."
elsif git.lines_of_code > 1000
  fail "PR has more than 1000 lines of modified code. Break it up into smaller ones."
end

# Notify the contributor if they can remove the code.
if git.deletions > git.insertions
  message  "Code Cleanup! 🎉"
end

# If these are all empty something has gone wrong, better to raise it in a comment.
if git.modified_files.empty? && git.added_files.empty? && git.deleted_files.empty?
  fail "This PR has no changes at all, this is likely an issue during development."
end

functionalChanges = git.modified_files.include? "**/main/**/*.kt"
testChanges = git.modified_files.include? "**/test/**/*.kt"
androidTestChanges = git.modified_files.include? "**/androidTest/**/*.kt"
docsChanges = git.modified_files.include? "*.md"

# Warn when the application source code has been modified, but the test sources have not.
if functionalChanges && !(testChanges || androidTestChanges)
  warn "Looks like this PR contains functional changes without a corresponding test"
end

# Thank when the documentation changes
if docsChanges
  message "Thank you for making documentation better :heart:!"
end

# Notify when we have outdated dependencies.
# In this case, gradle versions plugin have to be applied to the root project.
dependencyUpdatesReportFilePath = "build/dependencyUpdates/report.txt"

exceedDependenciesHeader = "The following dependencies exceed the version found at the milestone revision level:"
upgradeDependenciesHeader = "The following dependencies have later milestone versions:"
undeclaredDependenciesHeader = "Failed to compare versions for the following dependencies because they were declared without version:"
unresolvedDependenciesHeader = "Failed to determine the latest version for the following dependencies (use --info for details):"
gradleUpdatesHeader = "Gradle current updates:"

dependencyUpdatesReportLines = File.readlines(dependencyUpdatesReportFilePath)

areExceedDependenciesAvailable = dependencyUpdatesReportLines.grep(/#{exceedDependenciesHeader}/).any?
areUpgradeDependenciesAvailable = dependencyUpdatesReportLines.grep(/#{upgradeDependenciesHeader}/).any?
areUndeclaredDependenciesAvailable = dependencyUpdatesReportLines.grep(/#{undeclaredDependenciesHeader}/).any?
areUnresolvedDependenciesAvailable = dependencyUpdatesReportLines.grep(/#{unresolvedDependenciesHeader}/).any?
areGradleUpdatesAvailable = !dependencyUpdatesReportLines.find { |line| line =~ / - Gradle: \[/ && line =~ /\: UP-TO-DATE\]/ }

if areExceedDependenciesAvailable || areUpgradeDependenciesAvailable || areUndeclaredDependenciesAvailable || areUnresolvedDependenciesAvailable || areGradleUpdatesAvailable
  reportFileContent = File.open(dependencyUpdatesReportFilePath, "rb").read

  beginning = if areExceedDependenciesAvailable
      reportFileContent.index(exceedDependenciesHeader)
    elsif areUpgradeDependenciesAvailable
      reportFileContent.index(upgradeDependenciesHeader)
    elsif areUndeclaredDependenciesAvailable
      reportFileContent.index(undeclaredDependenciesHeader)
    elsif areUnresolvedDependenciesAvailable
      reportFileContent.index(unresolvedDependenciesHeader)
    elsif areGradleUpdatesAvailable
      reportFileContent.index(gradleUpdatesHeader)
    end

  ending = areGradleUpdatesAvailable ? 0 : reportFileContent.index(gradleUpdatesHeader)

  message reportFileContent.slice(beginning..(ending - 1))
end
