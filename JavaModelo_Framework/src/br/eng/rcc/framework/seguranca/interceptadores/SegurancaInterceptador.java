
package br.eng.rcc.framework.seguranca.interceptadores;

import br.eng.rcc.framework.seguranca.servicos.SegurancaServico;
import br.eng.rcc.framework.seguranca.anotacoes.Seguranca;
import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

@Seguranca
@Interceptor
public class SegurancaInterceptador {
    
    @Inject
    private SegurancaServico check;
    
    @AroundInvoke
    public Object seguranca(InvocationContext ctx) throws Exception{
        check.check( ctx.getMethod() );
        return ctx.proceed();
    }
    
}
