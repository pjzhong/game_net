#！ /bin/sh

workdir=$1
targetdir=$2

if [ ! $workdir ];then
    workdir=`pwd`
fi

if [ ! $targetdir ];then
    targetdir=`pwd`
fi

function dir() {
  for file in `ls $1`
  do
      path=$1"/"$file
      if [ -d $path ]
      then
          dir $path
      else
          if [ ${file##*.} == "proto" ];then
             `protoc -I=$1 --java_out=$targetdir $path`
             echo "compiled $path to $targetdir/$1"
          fi

      fi
  done
}

dir $workdir
