import {useState} from 'react';
import API,{setCredentials} from './axios';
import './account.css';
export default function Account(){
 const [user,setUser]=useState(null),[name,setName]=useState(''),[password,setPassword]=useState(''),[error,setError]=useState('');
 async function submit(register){try{setError('');if(register)await API.post('/register',{username:name,password});setCredentials({username:name,password});const {data}=await API.get('/me');setUser(data);setPassword('');}catch{setCredentials(undefined);setError('Unable to sign in. Check your credentials or choose another username.');}}
 return <section className="store-account" aria-label="Account">{user?<div className="store-account-session"><span>Signed in as {user.username}{user.admin?' (admin)':''}</span> <button onClick={()=>{setCredentials(undefined);setUser(null);window.location.assign('/');}}>Log out</button></div>:<details><summary>Your account <span>Sign in or create an account ↗</span></summary><form onSubmit={e=>{e.preventDefault();submit(false);}}><label>Username<input autoComplete="username" required value={name} onChange={e=>setName(e.target.value)}/></label><label>Password<input type="password" autoComplete="current-password" required value={password} onChange={e=>setPassword(e.target.value)}/></label><div className="store-account-actions"><button type="submit">Sign in</button><button type="button" onClick={()=>submit(true)}>Create customer account</button></div><small>Sign in before checkout or viewing your orders.</small></form></details>}{error&&<p role="alert">{error}</p>}</section>;
}
