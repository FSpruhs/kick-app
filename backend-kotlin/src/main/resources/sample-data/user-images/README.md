# Sample user images

Put one image per sample user in this directory.

Naming convention:
- `<userId>.jpg`
- `<userId>.jpeg`
- `<userId>.png`
- `<userId>.webp`
- `<userId>.svg`

Examples:
- `user-id-1.png`
- `user-id-2.jpg`
- `user-id-3.webp`

`UserImporter` loads images from `classpath:sample-data/user-images` and resolves users by file name prefix (`<userId>`).

