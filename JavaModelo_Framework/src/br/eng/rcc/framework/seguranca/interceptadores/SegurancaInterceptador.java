
package br.eng.rcc.framework.seguranca.interceptadores;

import br.eng.rcc.framework.seguranca.servicos.SegurancaServico;
import br.eng.rcc.framework.seguranca.anotacoes.SegurancaMetodo;
import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

@SegurancaMetodo
@Interceptor
public class SegurancaInterceptador {
    
    @Inject
    private SegurancaServico check;
    
    @AroundInvoke
    public Object seguranca(InvocationContext ctx) throws Exception{
      SegurancaMetodo segAnn = ctx.getMethod().getAnnotation(SegurancaMetodo.class);
      check.check( segAnn.value(), segAnn.grupo() );
      return ctx.proceed();
    }
    
}
