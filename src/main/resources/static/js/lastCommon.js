/*加密解密js*/
function aesEncrypt(data) {
    var key = CryptoJS.enc.Utf8.parse(ALLENCRYPTCODE);
    var encrypted = CryptoJS.AES.encrypt(data, key,
        {
            mode: CryptoJS.mode.ECB,
            padding: CryptoJS.pad.Pkcs7
        });
    return encrypted.toString();
}

function aesDeciphering(encrypted) {
    var key = CryptoJS.enc.Utf8.parse(ALLENCRYPTCODE);
    var decrypted = CryptoJS.AES.decrypt(encrypted.toString(), key,
        {
            mode: CryptoJS.mode.ECB,
            padding: CryptoJS.pad.Pkcs7
        });
    decrypted = CryptoJS.enc.Utf8.stringify(decrypted);
    // 转换为 utf8 字符串
    return decrypted;
}



function checkTime(creat_dt_begin,creat_dt_end,update_dt_begin,update_dt_end){
    if (null != creat_dt_begin && '' != creat_dt_begin) {
        if (creat_dt_begin.length != 10) {
            alertWarning('消息提醒：请输入规范的创建时间起始日期！');
            return false;
        }
        creat_dt_begin = creat_dt_begin.substring(0, 4) + creat_dt_begin.substring(5, 7) + creat_dt_begin.substring(8, 10);
        if (!isNumber(creat_dt_begin)) {
            alertWarning('消息提醒：请输入规范的创建时间起始日期！');
            return false;
        }
    }
    if (null != creat_dt_end && '' != creat_dt_end) {
        if (creat_dt_end.length != 10) {
            alertWarning('消息提醒：请输入规范的创建时间截止日期！');
            return false;
        }
        creat_dt_end = creat_dt_end.substring(0, 4) + creat_dt_end.substring(5, 7) + creat_dt_end.substring(8, 10);
        if (!isNumber(creat_dt_end)) {
            alertWarning('消息提醒：请输入规范的创建时间截止日期！');
            return false;
        }
    }
    if (null != creat_dt_end && '' != creat_dt_end && null != creat_dt_begin && '' != creat_dt_begin) {
        if (creat_dt_end < creat_dt_begin) {
            alertWarning('消息提醒：创建时间起始日期不能大于结束日期！');
            return false;
        }
    }

    if (null != update_dt_begin && '' != update_dt_begin) {
        if (update_dt_begin.length != 10) {
            alertWarning('消息提醒：请输入规范的更新时间起始日期！');
            return false;
        }
        update_dt_begin = update_dt_begin.substring(0, 4) + update_dt_begin.substring(5, 7) + update_dt_begin.substring(8, 10);
        if (!isNumber(update_dt_begin)) {
            alertWarning('消息提醒：请输入规范的更新时间起始日期！');
            return false;
        }
    }
    if (null != update_dt_end && '' != update_dt_end) {
        if (update_dt_end.length != 10) {
            alertWarning('消息提醒：请输入规范的更新时间截止日期！');
            return false;
        }
        update_dt_end = update_dt_end.substring(0, 4) + update_dt_end.substring(5, 7) + update_dt_end.substring(8, 10);
        if (!isNumber(update_dt_end)) {
            alertWarning('消息提醒：请输入规范的更新时间截止日期！');
            return false;
        }
    }
    if (null != update_dt_end && '' != update_dt_end && null != update_dt_begin && '' != update_dt_begin) {
        if (update_dt_end < update_dt_begin) {
            alertWarning('消息提醒：更新时间起始日期不能大于结束日期！');
            return false;
        }
    }
    return true;
}