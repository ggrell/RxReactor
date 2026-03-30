# RxReactor — Claude Notes

## Danger / Android Lint hybrid inline mode

`danger-android_lint` does not natively support hybrid mode (inline comments
for modified files + a summary comment for everything else). Calling `lint`
twice — once with `inline_mode: true` and once with `inline_mode: false` —
causes issues on modified files to appear twice (inline and in the table).

**If hybrid mode is ever needed**, implement it manually in the Dangerfile:
read each lint XML report directly, check each issue's file path against
`git.modified_files + git.added_files`, and route accordingly:

```ruby
require 'nokogiri'

Dir["**/build/reports/lint-results*.xml"].each do |report|
  doc = Nokogiri::XML(File.read(report))
  doc.xpath("//issue").each do |issue|
    location = issue.at_xpath("location")
    next unless location

    file    = location["file"].to_s.sub("#{Dir.pwd}/", "")
    line    = location["line"].to_i
    message = "[#{issue["id"]}] #{issue["message"]}"

    if (git.modified_files + git.added_files).include?(file)
      warn(message, file: file, line: line)   # inline comment
    else
      warn(message)                            # PR-level comment
    end
  end
end
```

The `oga` gem (already in the Gemfile) can be used instead of `nokogiri` if
preferred — swap `Nokogiri::XML` for `Oga.parse_xml`.
