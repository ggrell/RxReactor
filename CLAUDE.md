# RxReactor — Claude Notes

## Working style

Always confirm with the user before making any changes.

## Danger / Android Lint hybrid inline mode

`danger-android_lint` does not natively support hybrid mode (inline comments
for modified files + a summary comment for everything else). Calling `lint`
twice — once with `inline_mode: true` and once with `inline_mode: false` —
causes issues on modified files to appear twice (inline and in the table).

The Dangerfile already uses `oga` to preprocess the lint XML (for filtering
ignored issue IDs). To add hybrid mode, replace the `android_lint.lint` call
with direct Danger messaging after the XML is parsed:

```ruby
doc.xpath('//issue').each do |issue|
  location = issue.xpath('location').first
  next unless location

  file    = location.get('file').to_s.sub("#{Dir.pwd}/", '')
  line    = location.get('line').to_i
  message = "[#{issue.get('id')}] #{issue.get('message')}"

  if (git.modified_files + git.added_files).include?(file)
    warn(message, file: file, line: line)   # inline comment on diff
  else
    warn(message)                            # PR-level summary comment
  end
end
```

This replaces the `android_lint.lint(inline_mode: true)` call inside the
existing `Dir[lint_dir].each` block (after the IGNORED_LINT_IDS filtering).
