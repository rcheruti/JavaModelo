
package br.eng.rcc.seguranca.interceptadores;

import br.eng.rcc.seguranca.servicos.SegurancaServico;
import br.eng.rcc.seguranca.anotacoes.Seguranca;
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
        Seguranca klass = ctx.getMethod().getAnnotation(Seguranca.class);
        String valor = klass.value();
        String grupo = klass.grupo();
        check.check(valor,grupo);
        return ctx.proceed();
    }
    
}
