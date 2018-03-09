﻿define(function(require) {
	var Core = require('core/core');
	var Page = require('core/page');
	var Config = require('config');
	
	var OsInfoAdd = require('./osinfo.add');
	var OsInfoEdit = require('./osinfo.edit');
	var OsInfoRemove = require('./osinfo.remove');
    
	// 角色信息列表
	var OsInfo = Page.extend(function() {
		this.injecte([
	        new OsInfoAdd('osinfo_add'), 
	        new OsInfoEdit('osinfo_edit'), 
	        new OsInfoRemove('osinfo_remove')
	    ]);
		
		// @override
		this.load = function(panel) {
			panel.find('table').cdatagrid({
				// 必须要加此项!!
				controller: this
				
			});
		};
	});
	
	return OsInfo;
});