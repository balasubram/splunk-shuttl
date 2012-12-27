#!/bin/bash

# Copyright 2011 Splunk, Inc.                                                                       
#                                                                                                        
# Licensed under the Apache License, Version 2.0 (the "License");                                      
# you may not use this file except in compliance with the License.                                     
# You may obtain a copy of the License at                                                              
#                                                                                                        
#   http://www.apache.org/licenses/LICENSE-2.0                                                       
#                                                                                                        
# Unless required by applicable law or agreed to in writing, software                                  
# distributed under the License is distributed on an "AS IS" BASIS,                                    
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.                             
# See the License for the specific language governing permissions and                                  
# limitations under the License.

shuttl_dir=$(cd $(/usr/bin/dirname $0) && pwd)

set -e
set -u

source src/sh/set-ant-env.sh $shuttl_dir
$ANT_HOME/bin/ant dist create-spl-splunk-app-for-splunbase

