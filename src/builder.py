#!/usr/bin/env python3
import os
import re
import argparse


def combine_java_classes(folder, public_class_file, output_folders):
    """
    Combines all .java files in a folder recursively into one single file.
    Internal project imports are removed. Public class is preserved; other classes become package-private.
    The result is copied to all output folders.
    """
    imports = set()
    class_bodies = []
    java_files = []

    # Recursively collect all .java files
    for root, _, files in os.walk(folder):
        for f in files:
            if f.endswith(".java"):
                java_files.append(os.path.join(root, f))

    public_class_path = os.path.abspath(public_class_file)
    if not os.path.isfile(public_class_path):
        raise ValueError(f"Public class file {public_class_file} not found.")

    # Ensure public class comes first
    java_files_sorted = [public_class_path] + \
        [f for f in java_files if os.path.abspath(f) != public_class_path]

    # Convert project files to relative import paths for internal import detection
    project_imports = set()
    for f in java_files:
        rel_path = os.path.relpath(f, folder).replace(os.sep, '.')
        rel_import = re.sub(r'\.java$', '', rel_path)
        project_imports.add(rel_import)

    for filepath in java_files_sorted:
        with open(filepath, "r") as f:
            content = f.read()

        # Extract imports
        file_imports = re.findall(r'^\s*import\s+.*?;', content, re.MULTILINE)
        # Remove internal/project imports
        for imp in file_imports:
            if not any(imp.find(proj) != -1 for proj in project_imports):
                imports.add(imp)

        # Remove package statements and imports
        content_no_package = re.sub(
            r'^\s*package\s+.*?;\s*', '', content, flags=re.MULTILINE)
        content_no_imports = re.sub(
            r'^\s*import\s+.*?;\s*', '', content_no_package, flags=re.MULTILINE)

        # If public class, leave as-is
        if os.path.abspath(filepath) == public_class_path:
            class_bodies.append(content_no_imports.strip())
        else:
            # Remove 'public' from top-level classes
            content_no_public = re.sub(
                r'\bpublic\s+class\b', 'class', content_no_imports)
            class_bodies.append(content_no_public.strip())

    # Build final content
    combined_content = ""
    if imports:
        combined_content += "\n".join(sorted(imports)) + "\n\n"
    combined_content += "\n\n".join(class_bodies)

    # Write to all output folders
    for out_folder in output_folders:
        os.makedirs(out_folder, exist_ok=True)
        out_file_path = os.path.join(
            out_folder, os.path.basename(public_class_file))
        with open(out_file_path, "w") as f:
            f.write(combined_content)
        print(f"Written combined file to {out_file_path}")


if __name__ == "__main__":
    import sys
    parser = argparse.ArgumentParser(
        description="Combine Java classes into one file (strip internal imports).")
    parser.add_argument("--folder", default="graphit",
                        help="Folder to traverse recursively")
    parser.add_argument(
        "--public", default="graphit/Graphit.java", help="Path to public class file")
    parser.add_argument(
        "--out",
        default=["./examples/oneFilePlugNPlay"],  # wrap default in a list
        nargs="*",                               # zero or more folders
        help="Output folder(s)"
    )
    args = parser.parse_args()

    combine_java_classes(args.folder, args.public, args.out)
