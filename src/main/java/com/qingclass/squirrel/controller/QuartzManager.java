package com.qingclass.squirrel.controller;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
 
import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.Trigger;
import org.quartz.Trigger.TriggerState;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.quartz.TriggerUtils;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.impl.triggers.CronTriggerImpl;
 
public class QuartzManager {
 
	private static SchedulerFactory gSchedulerFactory = new StdSchedulerFactory();
	private static String JOB_GROUP_NAME = "MY_JOBGROUP_NAME";
	private static String TRIGGER_GROUP_NAME = "MY_TRIGGERGROUP_NAME";
 
	/**
	 * @Description: 添加一个定时任务，使用默认的任务组名，触发器名，触发器组名
	 * @param jobName
	 *            任务名
	 * @param cls
	 *            任务
	 * @param time
	 *            时间设置，参考quartz说明文档
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static void addJob(String jobName, Class cls, String cron) {
		try {
			Scheduler sched = gSchedulerFactory.getScheduler();
			JobDetail job = JobBuilder.newJob(cls).withIdentity(jobName, JOB_GROUP_NAME).build();
			// 表达式调度构建器
			CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(cron);
			// 按新的cronExpression表达式构建一个新的trigger
			Trigger trigger = TriggerBuilder.newTrigger().withIdentity(jobName, TRIGGER_GROUP_NAME)
					.withSchedule(scheduleBuilder).build();
 
			// 交给scheduler去调度
			sched.scheduleJob(job, trigger);
 
			// 启动
			if (!sched.isShutdown()) {
				sched.start();
				System.err.println("添加任务:"+jobName);
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
 
	/**
	 * @Description: 添加一个定时任务
	 * @param jobName
	 *            任务名
	 * @param jobGroupName
	 *            任务组名
	 * @param triggerName
	 *            触发器名
	 * @param triggerGroupName
	 *            触发器组名
	 * @param jobClass
	 *            任务
	 * @param time
	 *            时间设置，参考quartz说明文档
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static void addJob(String jobName, String jobGroupName, String triggerName, String triggerGroupName,
			Class jobClass, String cron) {
		try {
			Scheduler sched = gSchedulerFactory.getScheduler();
			JobDetail job = JobBuilder.newJob(jobClass).withIdentity(jobName, jobGroupName).build();
			// 表达式调度构建器
			CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(cron);
			// 按新的cronExpression表达式构建一个新的trigger
			Trigger trigger = TriggerBuilder.newTrigger().withIdentity(triggerName, triggerGroupName)
					.withSchedule(scheduleBuilder).build();
			sched.scheduleJob(job, trigger);
			// 启动
			if (!sched.isShutdown()) {
				sched.start();
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
 
	/**
	 * @Description: 修改一个任务的触发时间(使用默认的任务组名，触发器名，触发器组名)
	 * @param jobName
	 * @param time
	 */
	public static void modifyJobTime(String jobName, String cron) {
		TriggerKey triggerKey = TriggerKey.triggerKey(jobName, TRIGGER_GROUP_NAME);
 
		try {
			Scheduler sched = gSchedulerFactory.getScheduler();
			CronTrigger trigger = (CronTrigger) sched.getTrigger(triggerKey);
			if (trigger == null) {
				return;
			}
			String oldTime = trigger.getCronExpression();
			if (!oldTime.equalsIgnoreCase(cron)) {
				CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(cron);
				// 按新的cronExpression表达式重新构建trigger
				trigger = trigger.getTriggerBuilder().withIdentity(triggerKey).withSchedule(scheduleBuilder).build();
				// 按新的trigger重新设置job执行
				sched.rescheduleJob(triggerKey, trigger);
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
 
	
	/**
	 * @Description:修改任务，（可以修改任务名，任务类，触发时间）
	 * 		原理：移除原来的任务，添加新的任务	
	 * @param oldJobName ：原任务名
	 * @param jobName
	 * @param jobclass
	 * @param cron
	 * @date 2018年5月23日 上午9:13:10    
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static void modifyJob(String oldJobName, String jobName, Class jobclass, String cron) {
		/*
		 * removeJob(oldJobName); 
		 * addJob(jobName, jobclass, cron);
		 * System.err.println("修改任务"+oldJobName);
		 */
		TriggerKey triggerKey = TriggerKey.triggerKey(oldJobName, TRIGGER_GROUP_NAME);
		JobKey jobKey = JobKey.jobKey(oldJobName, JOB_GROUP_NAME);
		try {
			Scheduler sched = gSchedulerFactory.getScheduler();
			Trigger trigger = (Trigger) sched.getTrigger(triggerKey);
			if (trigger == null) {
				return;
			}
			sched.pauseTrigger(triggerKey);// 停止触发器
			sched.unscheduleJob(triggerKey);// 移除触发器
			sched.deleteJob(jobKey);// 删除任务
			System.err.println("移除任务:" + oldJobName);
 
			JobDetail job = JobBuilder.newJob(jobclass).withIdentity(jobName, JOB_GROUP_NAME).build();
			// 表达式调度构建器
			CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(cron);
			// 按新的cronExpression表达式构建一个新的trigger
			Trigger newTrigger = TriggerBuilder.newTrigger().withIdentity(jobName, TRIGGER_GROUP_NAME)
					.withSchedule(scheduleBuilder).build();
 
			// 交给scheduler去调度
			sched.scheduleJob(job, newTrigger);
 
			// 启动
			if (!sched.isShutdown()) {
				sched.start();
				System.err.println("添加新任务:" + jobName);
			}
			System.err.println("修改任务【" + oldJobName + "】为:" + jobName);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
 
	}
	
	
	/**
	 * @Description: 修改一个任务的触发时间
	 * @param triggerName
	 * @param triggerGroupName
	 * @param time
	 */
	public static void modifyJobTime(String triggerName, String triggerGroupName, String cron) {
		TriggerKey triggerKey = TriggerKey.triggerKey(triggerName, triggerGroupName);
		try {
			Scheduler sched = gSchedulerFactory.getScheduler();
			CronTrigger trigger = (CronTrigger) sched.getTrigger(triggerKey);
			if (trigger == null) {
				return;
			}
			String oldTime = trigger.getCronExpression();
			if (!oldTime.equalsIgnoreCase(cron)) {
				// trigger已存在，则更新相应的定时设置
				CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(cron);
				// 按新的cronExpression表达式重新构建trigger
				trigger = trigger.getTriggerBuilder().withIdentity(triggerKey).withSchedule(scheduleBuilder).build();
				// 按新的trigger重新设置job执行
				sched.resumeTrigger(triggerKey);
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
 
	/**
	 * @Description 移除一个任务(使用默认的任务组名，触发器名，触发器组名)
	 * @param jobName
	 */
	public static void removeJob(String jobName) {
		TriggerKey triggerKey = TriggerKey.triggerKey(jobName, TRIGGER_GROUP_NAME);
		JobKey jobKey = JobKey.jobKey(jobName, JOB_GROUP_NAME);
		try {
			Scheduler sched = gSchedulerFactory.getScheduler();
			Trigger trigger = (Trigger) sched.getTrigger(triggerKey);
			if (trigger == null) {
				return;
			}
			sched.pauseTrigger(triggerKey);// 停止触发器
			sched.unscheduleJob(triggerKey);// 移除触发器
			sched.deleteJob(jobKey);// 删除任务
			System.err.println("移除任务:"+jobName);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
 
	/**
	 * @Description: 移除一个任务
	 * @param jobName
	 * @param jobGroupName
	 * @param triggerName
	 * @param triggerGroupName
	 */
	public static void removeJob(String jobName, String jobGroupName, String triggerName, String triggerGroupName) {
		TriggerKey triggerKey = TriggerKey.triggerKey(jobName, triggerGroupName);
		JobKey jobKey = JobKey.jobKey(jobName, jobGroupName);
		try {
			Scheduler sched = gSchedulerFactory.getScheduler();
			sched.pauseTrigger(triggerKey);// 停止触发器
			sched.unscheduleJob(triggerKey);// 移除触发器
			sched.deleteJob(jobKey);// 删除任务
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
 
	/**
	 * @Description:暂停一个任务(使用默认组名)
	 * @param jobName
	 */
	public static void pauseJob(String jobName) {
		JobKey jobKey = JobKey.jobKey(jobName, JOB_GROUP_NAME);
		try {
			Scheduler sched = gSchedulerFactory.getScheduler();
			sched.pauseJob(jobKey);
		} catch (SchedulerException e) {
			e.printStackTrace();
		}
	}
 
	/**
	 * @Description:暂停一个任务
	 * @param jobName
	 * @param jobGroupName
	 */
	public static void pauseJob(String jobName, String jobGroupName) {
		JobKey jobKey = JobKey.jobKey(jobName, jobGroupName);
		try {
			Scheduler sched = gSchedulerFactory.getScheduler();
			sched.pauseJob(jobKey);
		} catch (SchedulerException e) {
			e.printStackTrace();
		}
	}
 
	/**
	 * @Description:恢复一个任务(使用默认组名)
	 * @param jobName
	 */
	public static void resumeJob(String jobName) {
		JobKey jobKey = JobKey.jobKey(jobName, JOB_GROUP_NAME);
		try {
			Scheduler sched = gSchedulerFactory.getScheduler();
			sched.resumeJob(jobKey);
		} catch (SchedulerException e) {
			e.printStackTrace();
		}
	}
 
	/**
	 * @Description:恢复一个任务
	 * @param jobName
	 * @param jobGroupName
	 * @date 2018年5月17日 上午9:56:09
	 */
	public static void resumeJob(String jobName, String jobGroupName) {
		JobKey jobKey = JobKey.jobKey(jobName, jobGroupName);
		try {
			Scheduler sched = gSchedulerFactory.getScheduler();
			sched.resumeJob(jobKey);
		} catch (SchedulerException e) {
			e.printStackTrace();
		}
	}
 
	/**
	 * @Description:启动所有定时任务
	 */
	public static void startJobs() {
		try {
			Scheduler sched = gSchedulerFactory.getScheduler();
			sched.start();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
 
	/**
	 * @Description 关闭所有定时任务
	 */
	public static void shutdownJobs() {
		try {
			Scheduler sched = gSchedulerFactory.getScheduler();
				if (!sched.isShutdown()) {
					sched.shutdown();
				}
		} catch (Exception e) {
				throw new RuntimeException(e);
		}
	}
 
	/**
	 * @Description: 立即运行任务，这里的立即运行，只会运行一次，方便测试时用。
	 * @param jobName
	 * @param jobGroupName
	 * @date 2018年5月17日 上午10:03:26
	 */
	public static void triggerJob(String jobName) {
		JobKey jobKey = JobKey.jobKey(jobName, JOB_GROUP_NAME);
		try {
			Scheduler sched = gSchedulerFactory.getScheduler();
			sched.triggerJob(jobKey);
		} catch (SchedulerException e) {
			e.printStackTrace();
		}
	}
 
	/**
	 * @Description: 立即运行任务，这里的立即运行，只会运行一次，方便测试时用。
	 * @param jobName
	 * @param jobGroupName
	 * @date 2018年5月17日 上午10:03:26
	 */
	public static void triggerJob(String jobName, String jobGroupName) {
		JobKey jobKey = JobKey.jobKey(jobName, jobGroupName);
		try {
			Scheduler sched = gSchedulerFactory.getScheduler();
			sched.triggerJob(jobKey);
		} catch (SchedulerException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * @Description: 获取任务状态
	 * 		NONE: 不存在
	 * 		NORMAL: 正常
	 * 		PAUSED: 暂停
	 * 		COMPLETE:完成
	 * 		ERROR : 错误
	 * 		BLOCKED : 阻塞
	 * @param jobName 触发器名
	 * @date 2018年5月21日 下午2:13:45    
	 */
	public static String getTriggerState(String jobName){
		TriggerKey triggerKey = TriggerKey.triggerKey(jobName, TRIGGER_GROUP_NAME);
		String name = null;
		try {
			Scheduler sched = gSchedulerFactory.getScheduler();
			TriggerState triggerState = sched.getTriggerState(triggerKey);
			name = triggerState.name();
		} catch (SchedulerException e) {
			e.printStackTrace();
		}
		return name;
	}
 
	/**
	 * @Description:获取最近8次执行时间
	 * @param cron
	 * @date 2018年5月24日 下午5:13:03    
	 */
	public static List<String> getRecentTriggerTime(String cron) {
		List<String> list = new ArrayList<String>();
		try {
			CronTriggerImpl cronTriggerImpl = new CronTriggerImpl();
			cronTriggerImpl.setCronExpression(cron);
			// 这个是重点，一行代码搞定
			List<Date> dates = TriggerUtils.computeFireTimes(cronTriggerImpl, null, 8);
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			for (Date date : dates) {
				list.add(dateFormat.format(date));
			}
			
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return list;
	}
	
}
