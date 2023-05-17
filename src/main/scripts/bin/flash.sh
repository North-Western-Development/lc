dd ibs=$(echo "12*1024*1024" | bc) count=1 if=/dev/zero of=$1
if [ "$3" = "yes" ]; then
	dd if=fw_jump.bin of=$1
	dd if=$2 of=$1 seek=2097152 oflag=seek_bytes
else
	dd if=$2 of=$1
fi
