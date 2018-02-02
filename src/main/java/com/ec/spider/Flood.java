package com.ec.spider;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import com.ec.common.spider.Spider;

public abstract class Flood<T> implements InitializingBean, DisposableBean {

	private ExecutorService executor;

	@Override
	public void afterPropertiesSet() throws Exception {

		this.executor = Executors.newSingleThreadExecutor(new ThreadFactory() {

			@Override
			public Thread newThread(Runnable r) {

				Thread t = new Thread(r);

				t.setUncaughtExceptionHandler(new UncaughtExceptionHandler() {

					@Override
					public void uncaughtException(Thread t, Throwable ex) {

						executor.execute(new Runer());
					}

				});

				return t;

			}
		});

		executor.execute(new Runer());

	}

	@Override
	public void destroy() throws Exception {
		this.executor.shutdown();
	}

	public abstract Map<String, T> getSpiders() ;
	
	
	private class Runer implements Runnable {
		@Override
		public void run() {
			while (!Thread.interrupted()) {
			//for(int i=0;i<3;i++){
				for(T spider:getSpiders().values()){
					((Spider)spider).crawl();
				}  
			}
		}
	}

	
}
