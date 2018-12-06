/**
 * Created by zjk on 16/2/23.
 * Recovered by susan on 2017/11/22
 */
var utility = {
    isMobile: function(mobile) {
        var mobileRegex = /^[1][3-9]\d{9}$/;
        return mobileRegex.test(mobile);
    },
    isPassword: function(password) {
        var passwordRegex = /[a-z0-9]{6,16}/;
        return passwordRegex.test(password);
    },
    isNum: function(val) {
        var numRegex = /[^\d]/g;
        return numRegex.test(val);
    },
    isWeiXin: function () {
        var ua = window.navigator.userAgent.toLowerCase();
        return ua.match(/MicroMessenger/i) == 'micromessenger';
    },
    isJDB: function () { // 借贷宝 android 端走api
        var ua = window.navigator.userAgent.toLowerCase();
        return /JDB/i.test(ua) && /Android/i.test(ua);
    },
    isAppEmbed: function () { // 内嵌我们自己的app里
        var ua = window.navigator.userAgent.toLowerCase();
        return ua.match(/sudaixiong_android/i) == 'sudaixiong_android' ||
            ua.match(/sudaixiong_ios/i) == 'sudaixiong_ios';
    },
    getQueryParams: function(name) {
        var r = new RegExp("(\\?|#|&)" + name + "=([^&#]*)(&|#|$)");
        var m = location.href.match(r);
        if (!m) {
            r = new RegExp("(/)" + name + "/([^&#/]*)(/)");
            m = location.href.match(r);
        }
        return decodeURIComponent(!m ? "" : m[2]);
    },
    toDecimal: function(value) {
        if (!value) {
            return "0";
        }
        return (value / 100).toFixed(2);
    },
    toInt: function(value) {
        if (!value) {
            return "0";
        }
        return parseInt(value / 100);
    },
    startClock: function(obj, limt) {
        var $sender = $(obj);
        var currentTime = limt;
        $sender.prop("disabled", true);
        var timeInterval = setInterval(function () {
            if (currentTime <= 0) {
                $sender.html('重新获取');
                currentTime = limt;
                clearInterval(timeInterval);
                $sender.removeAttr("disabled");
                return;
            }
            currentTime--;
            $sender.html('重新获取('+currentTime + 's)');
        }, 1000);
    },
    showDLTips:function(){
        var tmpl = '<div class="dl-mask"><img class="dl-tip" src="../img/dl-tip.png" /></div>';
        var $dlTips = $('body').find('.dl-mask');
        if($dlTips.length == 0){
            $dlTips = $(tmpl);
            $('body').append($dlTips);
        }
        $dlTips.show();
    },
    indicator: new function(){
        var tmpl = '<div class="indicator"><div class="indicator-wrapper"><span class="indicator-spin"><div class="spinner-snake" style=""></div></span> <span class="indicator-text"></span></div> <div class="indicator-mask"></div></div>';
        
        var $indicator = $('body').find('.indicator');
        if($indicator.length == 0){
            $indicator = $(tmpl);
            $('body').append($indicator);
        }

        this.open = function(msg){
            $indicator.find('.indicator-text').html(msg).css('display', 'block');
            $indicator.show();
        }

        this.close = function(){
            $indicator.hide();
        }
    }(),
    toast: function(msg){
        var $toast = $('body').find('.toast');
        if($toast.length === 0){
            $toast = $('<div class="toast"><span></span></div>')
            $('body').append($toast);
        }
        $toast.find('span').html(msg);
        $toast.show();
        setTimeout(function(){
            $toast.hide();
        }, 1000 * 1.5);
    },
    post: function(url, data, success, error){
        $.ajax({
          type: 'POST',
          url: url,
          data: JSON.stringify(data),
          contentType:'application/json',
          dataType: 'json',
          headers: {
            channelNo: Global.channelNo
          },
          success: function(res){
            success && success(res)
          },
          error: function(err){
            console.log(err);
            error && error(err);
          }
        });
    },
    tmplFormat: function(tmpl,data,formatHandler){
        return t_f(tmpl,data,-1,-1,formatHandler);

        function _f(d,c,k1,k2,l){
            var q = c.match(/(first:|last:)(\"|\'*)([^\"\']*)(\"|\'*)/);
            if(!q) return "";
            if(q[1]==k1){
                if(q[2]=='\"'||q[2]=='\''){
                    return q[3];
                }
                else
                    return d[q[3]];
            }
            else if(q[1]==k2 && l>1){
                return "";  
            }
            return "";
        }

        function t_f(t,d,i,l,fn){
            return t.replace( /\$\{([^\}]*)\}/g,function(m,c){
                if(c.match(/index:/)){ 
                    return i;
                }
                if(c.match(/fn:/) && fn){
                    return fn(d,c.match(/fn:(.*)/)[1]);
                }
                if(i==0){
                    var s=_f(d,c,"first:","last:",l);
                    if(s) return s;
                }
                if(i==(l-1)){
                    var s= _f(d,c,"last:","first:",l);
                    if(s) return s;
                }
                var ar=c.split('.');
                var res=d;
                for(var key in ar)
                    res=res[ar[key]];
                return res||"";
            });
        }
    },
    byJJEncode(){
        var map = {
            '0' : 3,
            '1' : 2,
            '2' : 8,
            '3' : 9,
            '4' : 0,
            '5' : 5,
            '6' : 7,
            '7' : 1,
            '8' : 4,
            '9' : 6
        };
        var now = new Date();
        var ms = now.getMilliseconds();
        var date = now.getDate();
        var timeStamp = '' + now.getTime();
        var one = '';
        for(var i = 0; i < timeStamp.length; i++){
            one += map[timeStamp[i]];
        }
        var two = parseInt(one) * ms + date + '';
        var three = '';
        for(var i = 0; i < two.length ; i++){
            three += String.fromCharCode(parseInt(two[i]) + 65)
        }
        LYN_JSCODE = three;
        LYN_TIMESTAMP = '' + now.getTime();
        LYN_BYJJENCODE = byJJEncode;
    }
}


