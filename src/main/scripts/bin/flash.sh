if [ "$1" == "" ]; then
    echo "Usage: flash.sh <path to device to flash> <path to firmware file> <use Open SBI (yes/no default: no)>"
    return 1
fi

if [ "$2" == "" ]; then
    echo "Usage: flash.sh <path to device to flash> <path to firmware file> <use Open SBI (yes/no default: no)>"
    return 1
fi

if [ "$3" == "" ]; then
    echo "Flashing without OpenSBI"
fi

if [ "$3" == "no" ]; then
    echo "Flashing without OpenSBI"
fi

echo "Proceeding will completely erase $1 and flash the firmware\n $2 to it. This process is irreversible."

read -p "Are you sure? " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]; then
    dd ibs=$(echo "12*1024*1024" | bc) count=1 if=/dev/zero of=$1
    if [ "$3" = "yes" ]; then
    	dd if=/mnt/builtin/firmware_files/fw_jump.bin of=$1
    	dd if=$2 of=$1 seek=2097152 oflag=seek_bytes
    else
    	dd if=$2 of=$1
    fi
fi

