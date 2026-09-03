# Black Duck Bridge Setup Checklist

Quick reference checklist for setting up Black Duck Bridge scanning in the Silverpeas Core project.

## ✅ Pre-Setup Requirements

- [ ] Access to Black Duck Hub/Server instance
- [ ] Admin permissions on GitHub repository
- [ ] Valid Black Duck license with scanning quotas
- [ ] Network connectivity from GitHub Actions to Black Duck server

## ✅ GitHub Repository Configuration

### Variables (Settings > Secrets and variables > Actions > Variables)
- [ ] `BLACKDUCK_URL` - Set to your Black Duck server URL (e.g., `https://your-blackduck-server.com`)

### Secrets (Settings > Secrets and variables > Actions > Secrets)  
- [ ] `BLACKDUCK_TOKEN` - API token from Black Duck Hub (see documentation for generation steps)

## ✅ File Setup

### Required Files (Already Created)
- [ ] `.github/workflows/black-duck-bridge.yml` - Main workflow file
- [ ] `bridge-config.yml` - Bridge configuration file
- [ ] `docs/BLACK_DUCK_BRIDGE_SETUP.md` - Comprehensive documentation

### Optional Files
- [ ] `.blackduck-exclude.txt` - Additional file exclusions (if needed)

## ✅ Black Duck Hub Configuration

- [ ] Project created in Black Duck Hub (will be auto-created on first scan)
- [ ] Appropriate permissions assigned to API token user
- [ ] Security policies configured
- [ ] License policies configured (if applicable)

## ✅ Initial Testing

### Test Manual Scan
1. [ ] Go to GitHub repository > Actions tab
2. [ ] Select "Black Duck Bridge Scan" workflow
3. [ ] Click "Run workflow"
4. [ ] Select "RAPID" scan type for quick test
5. [ ] Monitor execution and check for errors

### Test PR Integration
1. [ ] Create a test branch with minor change
2. [ ] Open pull request to main/master branch
3. [ ] Verify rapid scan runs automatically
4. [ ] Check PR comment is posted with results

## ✅ Verification Steps

- [ ] Scan completes successfully without authentication errors
- [ ] Results appear in Black Duck Hub under project "Silverpeas-Core"
- [ ] SARIF results uploaded to GitHub Security tab (for main branch scans)
- [ ] Artifacts generated and downloadable from workflow runs
- [ ] PR comments working for pull request scans

## ✅ Performance Optimization

- [ ] Review initial scan times and adjust timeouts if needed
- [ ] Check excluded files/directories are appropriate
- [ ] Verify Maven scope settings match project needs
- [ ] Consider customizing bridge-config.yml for project-specific needs

## ✅ Team Setup

- [ ] Share setup documentation with team
- [ ] Train team on viewing and interpreting scan results
- [ ] Establish process for handling policy violations
- [ ] Set up notifications for security alerts (optional)

## 🚨 Common Setup Issues

### Authentication Problems
- **Symptom**: "Unable to connect to Black Duck server"
- **Fix**: Verify BLACKDUCK_URL and BLACKDUCK_TOKEN are correct

### Build Failures
- **Symptom**: Maven build fails during detect phase
- **Fix**: Check Java version compatibility and Maven dependencies

### Timeout Issues  
- **Symptom**: Scan times out before completion
- **Fix**: Increase timeout values in workflow or use RAPID scan mode

### Policy Violations
- **Symptom**: Scan fails due to policy violations
- **Fix**: Review violations in Black Duck Hub, update components, or adjust policies

## 📋 Post-Setup Actions

### Immediate (First Week)
- [ ] Run baseline scan on main branch
- [ ] Review initial vulnerability findings
- [ ] Adjust policies if needed based on current project state
- [ ] Document any project-specific exclusions

### Ongoing (Monthly)
- [ ] Review and update vulnerable dependencies
- [ ] Monitor scan performance and optimize
- [ ] Audit policy violations and compliance status
- [ ] Update API token before expiration

### Quarterly  
- [ ] Review security metrics and trends
- [ ] Assess policy effectiveness
- [ ] Update documentation as needed
- [ ] Review team processes and training needs

## 📞 Support Resources

- **Documentation**: `docs/BLACK_DUCK_BRIDGE_SETUP.md`
- **Black Duck Help**: https://blackduck.docs.synopsys.com/
- **GitHub Actions Logs**: Available in workflow run details
- **Team Contact**: [Add your team contact information]

---

**Next Steps After Setup:**
1. Complete initial baseline scan
2. Review and address any high-priority vulnerabilities
3. Establish regular security review cadence
4. Monitor and optimize scan performance