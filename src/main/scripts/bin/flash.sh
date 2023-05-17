if [ "$1" == "" ]; then
    printf "Usage:\nflash.sh <path to flash mem> <path to firmware> <OpenSBI (yes/no default: no)>\n"
    return 1
fi

if [ "$2" == "" ]; then
    printf "Usage:\nflash.sh <path to flash mem> <path to firmware> <OpenSBI (yes/no default: no)>\n"
    return 1
fi

if [ "$3" == "" ]; then
    echo "Flashing without OpenSBI"
fi

if [ "$3" == "no" ]; then
    echo "Flashing without OpenSBI"
fi

printf "Proceeding will completely erase $1 and flash your custom firmware\nto it. This process is irreversible.\n"

read -p "Are you sure? " -n 1 -r
echo
case "$REPLY" in
    [yY][eE][sS]|[yY])
        printf "Erasing current firmware..."
        dd if=$2 of=$1 status=none # Work around for empty flash chips
        dd ibs=$(echo "12*1024*1024" | bc) count=1 if=/dev/zero of=$1 status=none
        printf " Done\n"
        if [ "$3" = "yes" ]; then
            printf "Flashing OpenSBI..."
            dd if=/mnt/builtin/firmware_files/fw_jump.bin of=$1 status=none
            printf " Done\n"
            printf "Flashing custom firmware..."
            dd if=$2 of=$1 seek=2097152 oflag=seek_bytes status=none
            printf " Done\n"
        else
            printf "Flashing custom firmware..."
            dd if=$2 of=$1 status=none
            printf " Done\n"
        fi
        ;;
    *)
esac

