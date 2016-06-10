<?php
header("Content-type: text/html; charset=utf-8");
class Jpush_send
{
    private
        $app_key = '70830845946fb85273946926';
    private $master_secret = 'bf92a9aa6fd11e932ba3421b';
    private $url = "https://api.jpush.cn/v3/push";

    public function send_pub($receiver, $extras, $m_time)
    {
        $result = $this->push($receiver, $extras,$m_time);
        $returnVal = array();
        if ($result) {
            $res_arr = json_decode($result, true);
            if (isset($res_arr['error'])) {
                $error_code = $res_arr['error']['code'];
                $returnVal['code'] = $error_code;
                $returnVal['flag']=$extras['flag'];
            } else {
                $returnVal['code'] = 200;
                $returnVal['flag']=$extras['flag'];
            }
        } else {
            $returnVal['code'] = 119;
            $returnVal['flag']=$extras['flag'];
        }
        echo json_encode($returnVal,JSON_UNESCAPED_UNICODE);
    }

    public function push($receiver,$extras, $m_time)
    {
        $base64 = base64_encode("$this->app_key:$this->master_secret");
        $header = array("Authorization:Basic $base64", "Content-Type:application/json");
        $data = array();
        $data['platform'] = 'all';
        $data['audience']=$receiver;
        $data['message'] = array(
            "content_type" => "text",
            "title"=>"msg",
            "msg_content"=>"Hi,JPush",
            "extras" =>$extras
        );
        $data['options'] = array(
            "sendno" => time(),
            "time_to_live" => $m_time
        );
        $param = json_encode($data);
        $res = $this->push_curl($param, $header);

        if ($res) {
            return $res;
        } else {
            return false;
        }
    }

    public function push_curl($param = "", $header = "")
    {
        if (empty($param)) {
            return false;
        }
        $postUrl = $this->url;
        $curlPost = $param;
        $ch = curl_init();
        curl_setopt($ch, CURLOPT_URL, $postUrl);
        curl_setopt($ch, CURLOPT_HEADER, 0);
        curl_setopt($ch, CURLOPT_RETURNTRANSFER, 1);
        curl_setopt($ch, CURLOPT_POST, 1);
        curl_setopt($ch, CURLOPT_POSTFIELDS, $curlPost);
        curl_setopt($ch, CURLOPT_HTTPHEADER, $header);
        curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, FALSE);
        curl_setopt($ch, CURLOPT_SSL_VERIFYHOST, FALSE);
        $data = curl_exec($ch);
        curl_close($ch);
        return $data;
    }
}

$flag=$_REQUEST['flag'];
if(empty($flag))return;

if(empty($_REQUEST['alias'])){
    $receiver='all';
}else{
    $alias=$_REQUEST['alias'];
    $receiver=array(
        'alias'=>$alias
    );
}

$extras['flag']=$flag;
if($flag=='NewUserEnter'){
    $extras['nickName']=$_REQUEST['nickName'];
	$extras['avatar']=$_REQUEST['avatar'];
	$extras['objectId']=$_REQUEST['objectId'];
}else if($flag=='UserExit'||$flag=='UserStart'){
	$extras['nickName']=$_REQUEST['nickName'];
	$extras['objectId']=$_REQUEST['objectId'];
}else if($flag=='DaoJiShi'){
    $extras['time']=10;
    $extras['questions']=$_REQUEST['questions'];
	$extras['roomId']=$_REQUEST['roomId'];
}else if($flag=='QiangDaSuccess'){
    $extras['nickName']=$_REQUEST['nickName'];
}else if($flag=='AnswerWrong'){
    $extras['nickName']=$_REQUEST['nickName'];
	$extras['nextNum']=$_REQUEST['nextNum'];
}else if($flag=='AnswerRight'){
    $extras['nickName']=$_REQUEST['nickName'];
	$extras['nextNum']=$_REQUEST['nextNum'];
}else if($flag=='GameOver'){
	$extras['roomId']=$_REQUEST['roomId'];
}
$jpush = new Jpush_send();
$jpush->send_pub($receiver,$extras,'0');

?>