usage() {
    echo "Usage: $0 <dir>"
    echo
    echo "  Automatically generate directory listing index page for <dir>"
    echo "  and all of its subdirectories."
}

if [ $# -ne 1 ]; then
    usage
    exit 1
fi

dir=$(find $1 -type d -exec echo {} \;)
for d in $dir; do
    if [ ! -f $d/index.html ]; then
        echo $d/index.md
        echo "---" > $d/index.md
        echo layout: page-list >> $d/index.md
        echo files: >> $d/index.md
        for f in $(ls -1pv $d); do
            if [ $f != index.md ]; then
                echo "- $f" >> $d/index.md
            fi
        done
        echo "---" >> $d/index.md
        echo >> $d/index.md
    fi
done
