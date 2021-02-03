package cn.buptleida.database;

import cn.buptleida.conf.Command;
import cn.buptleida.conf.Toast;
import cn.buptleida.util.ConvertUtil;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class ProxyInvocationHandler implements InvocationHandler {
    private Object target;

    public void setTarget(Object target) {
        this.target = target;
    }

    public Object getProxy(){
        return Proxy.newProxyInstance(this.getClass().getClassLoader(),target.getClass().getInterfaces(),this);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        //args参数分别是String[] params, Command cmd, Method method, RedisClient client
        String[] params = (String[]) args[0];
        Command cmd = (Command) args[1];
        Method concreteMethod = (Method) args[2];
        RedisClient client = (RedisClient) args[3];

        if(concreteMethod==null||cmd.getParamNum()!=params.length){
            client.msgReturn(Toast.PARAM_ERROR);
            return null;
        }


        Object[] concreteParams = null;
        if(cmd.getParamClasses()!=null){
            int len = cmd.getParamClasses().length;
            concreteParams = new Object[len];
            for(int i=0;i<len;++i){
                concreteParams[i] = ConvertUtil.convertFromStr(params[cmd.getParamNum()-len+i],cmd.getParamClasses()[i]);
            }
        }

        try {
            // Object msg = method.invoke(target,args);
            Object msg = concreteMethod.invoke(target, concreteParams);
            if(msg==null) client.msgReturn(Toast.SUCCESS);
            else client.msgReturn(msg);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            client.msgReturn(Toast.FAILURE);
        }

        return null;
    }

}
