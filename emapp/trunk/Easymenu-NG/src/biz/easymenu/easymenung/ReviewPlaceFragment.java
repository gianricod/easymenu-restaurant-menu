/*******************************************************************************
 * Copyright 2012 Gianrico D'Angelis  -- gianrico.dangelis@gmail.com
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package biz.easymenu.easymenung;

import java.text.DecimalFormat;
import java.util.ArrayList;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import biz.easymenu.easymenung.dbtables.DBRowOrder;

public class ReviewPlaceFragment extends Fragment {
	
	private TableLayout tlf = null;
	private TableLayout tlc = null;
	private ArrayList<DBRowOrder> ordf  = null;
	private ArrayList<DBRowOrder> ordc  = null;
	private Emrpc rpc = null;
	private EmPrefs emp = null;
	private ProgressBar pbar = null;
	private Button btnSend = null;
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
	}
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		View v = inflater.inflate(R.layout.reviewandplace,container,false);
		tlf = (TableLayout)v.findViewById(R.id.tableFixed);
		tlc = (TableLayout)v.findViewById(R.id.tableCarte);
		pbar = (ProgressBar)v.findViewById(R.id.pBarOrder);
		btnSend = (Button)v.findViewById(R.id.btnSend);
		btnSend.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				pbar.setVisibility(View.VISIBLE);
				new Thread(new SendOrderRun()).start();
			}
		});
		
		new Thread(new BillItemsRun()).start();
		return v;
	}
	
	
	private Handler itemsHandler = new Handler(){
		public void handleMessage(Message mg){
			TableRow headerf = (TableRow)getActivity().getLayoutInflater().inflate(R.layout.orderheader,null);
			((TextView)headerf.findViewById(R.id.textHeader)).setText("Fixed Menu");
			tlf.addView(headerf);
			if(ordf.size()==0){
				TableRow row = (TableRow)getActivity().getLayoutInflater().inflate(R.layout.orderrow,null);
				((TextView)row.findViewById(R.id.textLabel)).setText("No items");
				tlf.addView(row);
			}
			for(DBRowOrder item:ordf){
				try{
				TableRow row = (TableRow)getActivity().getLayoutInflater().inflate(R.layout.orderrow,null);
				
				int iNum = Integer.parseInt(item.getNumber());
				int iPrice = (int)(Double.parseDouble(item.getPrice())*100);
				
				int itot = iNum*iPrice;
				double tot = itot/100.0;
				
				DecimalFormat df = new DecimalFormat("0.00");
				
				((TextView)row.findViewById(R.id.textNumber)).setText(item.getNumber());
				((TextView)row.findViewById(R.id.textLabel)).setText(item.getLabel());
				//((TextView)row.findViewById(R.id.textPrice)).setText(df.format(tot));
				tlf.addView(row);
				}catch (ClassCastException e){
					Log.e("CLASS CAST",e.getMessage());
				}
			}
			TableRow headerc = (TableRow)getActivity().getLayoutInflater().inflate(R.layout.orderheader,null);
			((TextView)headerc.findViewById(R.id.textHeader)).setText("Extras");
			tlc.addView(headerc);
			if(ordc.size()==0){
				TableRow row = (TableRow)getActivity().getLayoutInflater().inflate(R.layout.orderrow,null);
				((TextView)row.findViewById(R.id.textLabel)).setText("No items");
				tlc.addView(row);
			}
				
			for(DBRowOrder item:ordc){
				try{
				TableRow row = (TableRow)getActivity().getLayoutInflater().inflate(R.layout.orderrow,null);
				
				int iNum = Integer.parseInt(item.getNumber());
				int iPrice = (int)(Double.parseDouble(item.getPrice())*100);
				
				int itot = iNum*iPrice;
				double tot = itot/100.0;
				
				DecimalFormat df = new DecimalFormat("0.00");
				
				((TextView)row.findViewById(R.id.textNumber)).setText(item.getNumber());
				((TextView)row.findViewById(R.id.textLabel)).setText(item.getLabel());
				((TextView)row.findViewById(R.id.textPrice)).setText(df.format(tot)+" "+emp.getValue("currency"));
				tlc.addView(row);
				}catch (ClassCastException e){
					Log.e("CLASS CAST",e.getMessage());
				}
			}
			pbar.setVisibility(View.GONE);
		}
	};
	
	private class BillItemsRun implements Runnable{

		@Override
		public void run() {
			rpc = new Emrpc(getActivity());
			emp = new EmPrefs(getActivity());
			ordf = rpc.getOrderItems(Emrpc.T_FIXED,emp.getSid());
			ordc = rpc.getOrderItems(Emrpc.T_CARTE,emp.getSid());
			itemsHandler.sendEmptyMessage(0);
		}
		
	}
	
	private Handler sendHandler = new Handler(){
		public void handleMessage(Message msg){
			if(msg.what == 0){
				FragmentManager fm = getActivity().getSupportFragmentManager();
		    	FragmentTransaction ft ;
		    	if(fm.findFragmentByTag("rightfragment")!=null){
	    			ft = fm.beginTransaction();
	    			ft.setCustomAnimations(android.R.anim.fade_in,
	    	                android.R.anim.fade_out);
	    			ft.remove(fm.findFragmentByTag("rightfragment"));
	    			ft.commit();
	    		}
		    	ErrorFragment f = new ErrorFragment("Order Sent"); 
            	Fragment prev = fm.findFragmentByTag("errorDialog");
            	ft = fm.beginTransaction();
                if (prev != null) {
                    ft.remove(prev);
                    ft.commit();
                }
                f.show(ft, "errorDialog");
			}
			//pbar.setVisibility(View.GONE);
			else{
				FragmentManager fm = getActivity().getSupportFragmentManager();
		    	FragmentTransaction ft ;
		    	ErrorFragment f = new ErrorFragment("Error"); 
            	Fragment prev = fm.findFragmentByTag("errorDialog");
            	ft = fm.beginTransaction();
                if (prev != null) {
                    ft.remove(prev);
                    ft.commit();
                }
                f.show(ft, "errorDialog");
                pbar.setVisibility(View.GONE);
			}
		}
	};
	
	private class SendOrderRun implements Runnable{

		@Override
		public void run() {
			boolean result;
			try{
			result = rpc.sendOrder(emp.getSid());
			}catch(Exception e){
				sendHandler.sendEmptyMessage(Integer.parseInt(e.getMessage()));
				return;
			}
			if(result)
				sendHandler.sendEmptyMessage(0);
			else
				sendHandler.sendEmptyMessage(1);
		}
		
	}

}
