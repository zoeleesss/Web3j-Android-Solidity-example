<?php 

function getRareRank($all_shapes,$all_colors){

    $repeat_shapes = 0;
    $repeat_shape_1 = 0;
    $repeat_shape_2 = 0;
    $repeat_shape_3 = 0;
    $repeat_colors = 0;
    $repeat_color_1 = 0;
    $repeat_color_2 = 0;
    $repeat_color_3 = 0;
    $repeat_color_4 = 0;
    $repeat_color_5 = 0;
    $repeat_color_6 = 0;
    $repeat_color_7 = 0;
    $repeat_color_8 = 0;

    for ($i = 0;$i<count($all_shapes);$i++)
    {
        if ($all_shapes[$i]=='shape_1') $repeat_shape_1++;
        else if ($all_shapes[$i]=='shape_2') $repeat_shape_2++;
        else if ($all_shapes[$i]=='shape_3') $repeat_shape_3++;
    }

    $repeat_shapes = max($repeat_shape_1,$repeat_shape_2,$repeat_shape_3);

    for ($i = 0;$i<count($all_colors);$i++)
    {
        if ($all_colors[$i]=='color_1') $repeat_color_1++;
        else if ($all_colors[$i]=='color_2') $repeat_color_2++;
        else if ($all_colors[$i]=='color_3') $repeat_color_3++;
        else if ($all_colors[$i]=='color_4') $repeat_color_4++;
        else if ($all_colors[$i]=='color_5') $repeat_color_5++;
        else if ($all_colors[$i]=='color_6') $repeat_color_6++;
        else if ($all_colors[$i]=='color_7') $repeat_color_7++;
        else if ($all_colors[$i]=='color_8') $repeat_color_8++;
    }

    $repeat_colors = max($repeat_color_1,$repeat_color_2,$repeat_color_3,$repeat_color_4,$repeat_color_5,$repeat_color_6,$repeat_color_7,$repeat_color_8);

    $scores = $repeat_shapes * 3 + $repeat_colors * 8;
    $rare = 0;

    // sort
    if ($scores <= 20 + 11 ){
        $rare = 1;
    }else if ($scores < 27+ 11){
        $rare = 2;
    }else if ($scores < 33+ 11){
        $rare = 3;
    }else if ($scores < 41+ 11){
        $rare = 4;
    }else if ($scores < 49+ 11){
        $rare = 5;
    }else if ($scores < 57+ 11){
        $rare = 6;
    }else if ($scores < 65+ 11){
        $rare = 7;
    }else if ($scores >= 66+ 11){
        $rare = 8;
    }

    return $rare;
}

function getRareName($rare){
    $names = ['普通','稀有','卓越','史诗','神话','传说','超神','宇宙之主'];
    return $names[$rare];
}



$gene = $_GET['gene'];

if ($gene == "")
{
$gene = sha1("hello".$_SERVER['REMOTE_ADDR']).sha1(time);
//echo $gene;
}
$shapes = ["shape_1","shape_2","shape_3"];
$colors = ["color_1","color_2","color_3","color_4","color_5","color_6","color_7","color_8"];
$parts  = ["fin.png","tail.png","bottom.png","body.png","streak.png","head.png","middle.png"];

$all_shapes = [];
$all_colors = [];

for ($i=0;$i<count($parts);$i++)
{
    $part_i = substr($gene,$i*10,10);
    $shape_i = intval(substr($part_i,0,3)) % count($shapes) | 0;
    $color_i = intval(substr($part_i,3,3)) % count($colors) | 0;

    $shape = $shapes[$shape_i];
    array_push($all_shapes,$shape);
    $color = $colors[$color_i];
    array_push($all_colors,$color);

}
$rare = getRareName(getRareRank($all_shapes,$all_colors));
header("Content-type: text/html; charset=utf-8"); 

echo $rare;

?>